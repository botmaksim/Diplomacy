package com.diplomacy.logic.save;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import com.diplomacy.logic.save.gameHistory.saveData.SaveContainer;

public class SaveManager {

    private static final long MAGIC_NUMBER = 0x4449504C4F4D4143L;
    private static final String EXTENSION = ".dip";

    public void save(SaveContainer container, String fileName) throws IOException {
        File file = new File(fileName + EXTENSION);

        try (FileOutputStream fos = new FileOutputStream(file); CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32()); ObjectOutputStream oos = new ObjectOutputStream(cos)) {

            oos.writeLong(MAGIC_NUMBER);

            oos.writeObject(container);

            long checksum = cos.getChecksum().getValue();
            oos.writeLong(checksum);

            oos.flush();
        }
    }

    public SaveContainer load(File file) throws SaveException {
        if (!file.exists()) {
            throw new SaveException("Файл не найден.");
        }

        try (FileInputStream fis = new FileInputStream(file); CheckedInputStream cis = new CheckedInputStream(fis, new CRC32()); ObjectInputStream ois = new ObjectInputStream(cis)) {

            long magic = ois.readLong();
            if (magic != MAGIC_NUMBER) {
                throw new SaveException("Неверная сигнатура.");
            }

            Object obj = ois.readObject();
            if (!(obj instanceof SaveContainer)) {
                throw new SaveException("Файл содержит некорректный тип данных.");
            }

            long calculatedChecksum = cis.getChecksum().getValue();
            long storedChecksum = ois.readLong();
            if (calculatedChecksum != storedChecksum) {
                throw new SaveException("Файл поврежден.");
            }

            return (SaveContainer) obj;

        } catch (ClassNotFoundException | InvalidClassException e) {
            throw new SaveException("Файл несовместим.");
        } catch (StreamCorruptedException | EOFException e) {
            throw new SaveException("Файл поврежден.");
        } catch (IOException e) {
            throw new SaveException("Ошибка ввода-вывода при чтении файла.");
        }
    }
}

class SaveException extends Exception {

    public SaveException(String message) {
        super(message);
    }
}
