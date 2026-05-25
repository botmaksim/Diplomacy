import json
from collections import OrderedDict

def clean_adjacency(adj_list, province_ids):
    """Оставляет в списке смежности только идентификаторы провинций."""
    return [item for item in adj_list if item in province_ids]

def sort_and_clean_map(input_file, output_file):
    # Загрузка данных
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    provinces = data['provinces']
    province_ids = set(provinces.keys())
    
    # Очистка adjacency во всех локациях
    for pid, info in provinces.items():
        locations = info.get('locations', {})
        for loc_name, loc_data in locations.items():
            if 'adjacency' in loc_data:
                loc_data['adjacency'] = clean_adjacency(loc_data['adjacency'], province_ids)
    
    # Сортировка провинций по hex_ID
    sorted_provinces = OrderedDict()
    for pid, info in sorted(provinces.items(), key=lambda x: x[1]['hex_ID']):
        sorted_provinces[pid] = info
    
    data['provinces'] = sorted_provinces
    
    # Сохранение
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    
    print(f"Обработка завершена. Результат сохранён в {output_file}")

if __name__ == "__main__":
    sort_and_clean_map('europe1900MapData.json', 'diplomacy_map_cleaned.json')

# Пример использования:
# sort_and_clean_map('diplomacy_map.json', 'diplomacy_map_cleaned.json')