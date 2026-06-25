package com.diplomacy.logic.gameControllingUnits.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.MovementPhaseOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.BeConvoyedOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.ConvoyOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.HoldOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.MoveOrder;
import com.diplomacy.logic.orders.movementPhaseOrders.SupportOrder;
import com.diplomacy.logic.units.Unit;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.utils.ConvoyHelper;

public class Adjudicator {

    public enum Status {
        UNRESOLVED, SUCCESS, FAILS
    }

    private final List<MovementPhaseOrder> allOrders = new ArrayList<>();
    private final Map<Province, MovementPhaseOrder> orderMap = new HashMap<>();
    private final Map<MovementPhaseOrder, Status> statusMap = new HashMap<>();

    public void resolve(List<MovementPhaseOrder> orders, List<Unit> allUnits) {
        allOrders.addAll(orders);

        Set<Unit> orderedUnits = orders.stream()
                .map(o -> o.getTarget().getParentProvince().getOccupyingUnit())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Unit u : allUnits) {
            if (!orderedUnits.contains(u)) {
                HoldOrder hold = new HoldOrder(u.getLocation());
                allOrders.add(hold);
            }
        }

        for (MovementPhaseOrder o : allOrders) {
            orderMap.put(o.getTarget().getParentProvince(), o);
            statusMap.put(o, Status.UNRESOLVED);
            o.setExecutable(false);
        }

        boolean changed = true;
        int maxIter = 1000;
        while (changed && maxIter-- > 0) {
            changed = false;
            for (MovementPhaseOrder o : allOrders) {
                if (statusMap.get(o) != Status.UNRESOLVED) {
                    continue;
                }

                if (o instanceof ConvoyOrder || o instanceof HoldOrder) {
                    if (isDislodged(o)) {
                        statusMap.put(o, Status.FAILS);
                        changed = true;
                    } else if (isGuaranteedNotDislodged(o)) {
                        statusMap.put(o, Status.SUCCESS);
                        changed = true;
                    }
                } else if (o instanceof SupportOrder s) {
                    if (isDislodged(s) || isSupportCut(s)) {
                        statusMap.put(o, Status.FAILS);
                        changed = true;
                    } else if (isGuaranteedNotDislodged(s) && isGuaranteedNotCut(s)) {
                        statusMap.put(o, Status.SUCCESS);
                        changed = true;
                    }
                } else if (o instanceof MoveOrder || o instanceof BeConvoyedOrder) {
                    Province dest = getDest(o);

                    if (o instanceof BeConvoyedOrder bc) {
                        if (!hasValidConvoyPath(bc)) {
                            statusMap.put(o, Status.FAILS);
                            changed = true;
                            continue;
                        }
                    }

                    int minStr = getMinAttackStrength(o);
                    int maxStr = getMaxAttackStrength(o);
                    int minOpp = getMinPreventStrength(o);
                    int maxOpp = getMaxPreventStrength(o);
                    int minHold = getMinHoldStrength(dest);
                    int maxHold = getMaxHoldStrength(dest);

                    MovementPhaseOrder destOrder = orderMap.get(dest);
                    boolean h2h = isHeadToHead(o, destOrder);

                    if (h2h) {
                        int minH2H = getMinAttackStrength(destOrder);
                        int maxH2H = getMaxAttackStrength(destOrder);
                        if (minStr > maxH2H && minStr > maxOpp) {
                            statusMap.put(o, Status.SUCCESS);
                            changed = true;
                        } else if (maxStr <= minH2H || maxStr <= minOpp) {
                            statusMap.put(o, Status.FAILS);
                            changed = true;
                        }
                    } else {
                        if (minStr > maxHold && minStr > maxOpp) {
                            if (isSelfDislodgement(o, dest)) {
                                statusMap.put(o, Status.FAILS);
                                changed = true;
                            } else {
                                statusMap.put(o, Status.SUCCESS);
                                changed = true;
                            }
                        } else if (maxStr <= minHold || maxStr <= minOpp) {
                            statusMap.put(o, Status.FAILS);
                            changed = true;
                        }
                    }
                }
            }
        }

        for (MovementPhaseOrder o : allOrders) {
            if (statusMap.get(o) == Status.UNRESOLVED) {
                statusMap.put(o, Status.FAILS);
            }
        }
        for (MovementPhaseOrder o : allOrders) {
            if (statusMap.get(o) == Status.FAILS && o instanceof MoveOrder) {
                Province dest = getDest(o);
                if (dest != null) {
                    dest.setBattled(true);
                }
            }
        }

        // Apply successes to original orders list
        for (MovementPhaseOrder o : orders) {
            if (statusMap.get(o) == Status.SUCCESS) {
                o.setExecutable(true);
                Province dest = getDest(o);
                if (dest != null) {
                    dest.setBattled(true);
                    if (dest.isOccupied()) {
                        Unit occ = dest.getOccupyingUnit();
                        if (!occ.getOwner().equals(o.getTarget().getParentProvince().getOccupyingUnit().getOwner())) {
                            occ.setRetreating(true);
                        }
                    }
                }
            }
        }
    }

    private boolean isSelfDislodgement(MovementPhaseOrder o, Province dest) {
        Unit targetUnit = dest.getOccupyingUnit();
        Unit movingUnit = o.getTarget().getParentProvince().getOccupyingUnit();
        return targetUnit != null && movingUnit != null && targetUnit.getOwner().equals(movingUnit.getOwner());
    }

    private Province getDest(MovementPhaseOrder o) {
        if (o instanceof MoveOrder m) {
            return m.getDestination().getParentProvince();
        }
        if (o instanceof BeConvoyedOrder bc) {
            return bc.getDestination().getParentProvince();
        }
        return null;
    }

    private boolean isHeadToHead(MovementPhaseOrder o1, MovementPhaseOrder o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        Province d1 = getDest(o1);
        Province d2 = getDest(o2);
        return d1 != null && d2 != null
                && d1.equals(o2.getTarget().getParentProvince())
                && d2.equals(o1.getTarget().getParentProvince())
                && !(o1 instanceof BeConvoyedOrder) && !(o2 instanceof BeConvoyedOrder);
    }

    private boolean hasValidConvoyPath(BeConvoyedOrder bc) {
        ConvoyHelper helper = new ConvoyHelper();
        Unit u = bc.getTarget().getParentProvince().getOccupyingUnit();
        if (u instanceof Army army) {
            return helper.CanConvoy(army.getLocation(), bc.getDestination().getParentProvince(), army);
        }
        return false;
    }

    private boolean isDislodged(MovementPhaseOrder o) {
        int holdStr = getMaxHoldStrength(o.getTarget().getParentProvince());
        for (MovementPhaseOrder attacker : getAttackers(o.getTarget().getParentProvince())) {
            if (getMinAttackStrength(attacker) > holdStr && !isSelfDislodgement(attacker, o.getTarget().getParentProvince())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGuaranteedNotDislodged(MovementPhaseOrder o) {
        int holdStr = getMinHoldStrength(o.getTarget().getParentProvince());
        for (MovementPhaseOrder attacker : getAttackers(o.getTarget().getParentProvince())) {
            if (getMaxAttackStrength(attacker) > holdStr) {
                return false;
            }
        }
        return true;
    }

    private boolean isSupportCut(SupportOrder s) {
        for (MovementPhaseOrder attacker : getAttackers(s.getTarget().getParentProvince())) {
            if (statusMap.get(attacker) == Status.FAILS) {
                continue;
            }
            Unit attackerUnit = attacker.getTarget().getParentProvince().getOccupyingUnit();
            Unit supporterUnit = s.getTarget().getParentProvince().getOccupyingUnit();

            if (attackerUnit.getOwner().equals(supporterUnit.getOwner())) {
                continue;
            }

            if (attacker instanceof BeConvoyedOrder bc) {
                if (statusMap.get(bc) == Status.FAILS) {
                    continue;
                }
            }

            Province attackSource = attacker.getTarget().getParentProvince();
            Province supportTarget = s.getDestination().getParentProvince();

            if (attackSource.equals(supportTarget)) {
                if (isDislodged(s)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isGuaranteedNotCut(SupportOrder s) {
        for (MovementPhaseOrder attacker : getAttackers(s.getTarget().getParentProvince())) {
            if (statusMap.get(attacker) == Status.FAILS) {
                continue;
            }
            Unit attackerUnit = attacker.getTarget().getParentProvince().getOccupyingUnit();
            Unit supporterUnit = s.getTarget().getParentProvince().getOccupyingUnit();
            if (attackerUnit.getOwner().equals(supporterUnit.getOwner())) {
                continue;
            }

            Province attackSource = attacker.getTarget().getParentProvince();
            Province supportTarget = s.getDestination().getParentProvince();

            if (!attackSource.equals(supportTarget)) {
                return false; // There's a non-failing attack from elsewhere
            }
        }
        return true;
    }

    private List<MovementPhaseOrder> getAttackers(Province dest) {
        return allOrders.stream()
                .filter(o -> dest.equals(getDest(o)))
                .collect(Collectors.toList());
    }

    private int getMinAttackStrength(MovementPhaseOrder o) {
        int str = 1;
        for (MovementPhaseOrder s : getSupports(o)) {
            if (statusMap.get(s) == Status.SUCCESS) {
                str++;
            }
        }
        return str;
    }

    private int getMaxAttackStrength(MovementPhaseOrder o) {
        int str = 1;
        for (MovementPhaseOrder s : getSupports(o)) {
            if (statusMap.get(s) != Status.FAILS) {
                str++;
            }
        }
        return str;
    }

    private int getMinHoldStrength(Province p) {
        MovementPhaseOrder o = orderMap.get(p);
        if (o == null || getDest(o) != null) {
            return 0;
        }
        int str = 1;
        for (MovementPhaseOrder s : getSupports(o)) {
            if (statusMap.get(s) == Status.SUCCESS) {
                str++;
            }
        }
        return str;
    }

    private int getMaxHoldStrength(Province p) {
        MovementPhaseOrder o = orderMap.get(p);
        if (o == null || getDest(o) != null) {
            return 0;
        }
        int str = 1;
        for (MovementPhaseOrder s : getSupports(o)) {
            if (statusMap.get(s) != Status.FAILS) {
                str++;
            }
        }
        return str;
    }

    private int getMinPreventStrength(MovementPhaseOrder o) {
        int max = 0;
        for (MovementPhaseOrder other : getAttackers(getDest(o))) {
            if (other.equals(o)) {
                continue;
            }
            if (isHeadToHead(other, orderMap.get(getDest(other)))) {
                continue;
            }
            if (statusMap.get(other) == Status.SUCCESS) {
                max = Math.max(max, getMinAttackStrength(other));
            }
        }
        return max;
    }

    private int getMaxPreventStrength(MovementPhaseOrder o) {
        int max = 0;
        for (MovementPhaseOrder other : getAttackers(getDest(o))) {
            if (other.equals(o)) {
                continue;
            }
            if (statusMap.get(other) != Status.FAILS) {
                max = Math.max(max, getMaxAttackStrength(other));
            }
        }
        return max;
    }

    private List<SupportOrder> getSupports(MovementPhaseOrder supported) {
        return allOrders.stream()
                .filter(o -> o instanceof SupportOrder)
                .map(o -> (SupportOrder) o)
                .filter(s -> {
                    Province supDest = s.getDestination().getParentProvince();
                    Province targetDest = getDest(supported) != null ? getDest(supported) : supported.getTarget().getParentProvince();
                    return supDest.equals(targetDest) && s.getSupportedUnit().getParentProvince().equals(supported.getTarget().getParentProvince());
                })
                .collect(Collectors.toList());
    }
}
