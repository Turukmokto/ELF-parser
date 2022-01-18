package evm;

import java.io.*;

public class elfReader implements AutoCloseable {
    private int position; // индекс следующего байта для чтения
    private InputStream reader; // ввод данных
    private final String fileName; //название входного файла

    public elfReader(String fileName) throws IOException {
        this.fileName = fileName;
        this.reader = new BufferedInputStream(new FileInputStream(fileName));
        this.position = 0;
        if (read(4) != 1) {
            throw new IOException("Incorrect file: it is not 32-bit object file");
        }
        if (read(5) != 1) {
            throw new IOException("Incorrect file: method for encoding data is not Little Endian");
        }
        if (read(18, 2) != 0xF3) {
            throw new IOException("Incorrect file: the architecture of the hardware platform is not RISC-V");
        }
    }

    public char read(int ind) throws IOException {
        move(ind);
        position++;
        return (char) reader.read();
    }

    int read(int ind, int size) throws IOException {
        int res = 0;
        int shift = 0;
        move(ind);
        for (int i = 0; i < size; i++) {
            res += (reader.read() << shift);
            shift += 8;
        }
        position += size;
        return res;
    }

    public void close() throws IOException {
        reader.close();
    }

    private void move(int ind) throws IOException {
        if (ind < position) {
            reader.close();
            reader = new BufferedInputStream(new FileInputStream(fileName));
            position = 0;
        }
        while (position < ind) {
            reader.read();
            position++;
        }
    }
}
