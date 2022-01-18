package evm;

import java.io.*;
import java.util.*;

public class hw4 {
    private static void printMem(int mem, PrintStream out) {
        out.printf("%08x:\t", mem);
    }
    private static void printMem(int mem, String mark, PrintStream out) {
        out.printf("%08x: <%s>\t", mem, mark);
    }
    private static String title(int name, char[] stringTable) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            if (stringTable[name + i] == '\0') {
                break;
            }
            sb.append(stringTable[name + i]);
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        PrintStream out = System.out;
        try (elfReader in = new elfReader(args[0])) {
            if (args.length > 1) {
                out = new PrintStream(args[1]);
            }
            int e_shoff = in.read(32, 2);
            int e_shnum = in.read(48, 2);
            int e_shstrndx = in.read(50, 2);
            int sh_name;
            int sh_type;
            int sh_addr;
            int sh_offset;
            int sh_size;
            int st_name;
            int st_value;
            int st_info;
            sh_offset = in.read(e_shoff + 40 * e_shstrndx + 16, 4);
            sh_size = in.read(e_shoff + 40 * e_shstrndx + 20, 4);
            char[] setOfStrings = new char[sh_size];
            for (int i = 0; i < sh_size; i++) {
                setOfStrings[i] = in.read(sh_offset + i);
            }
            Map<Integer, String> marks = new HashMap<>();
            for (int block = 0; block < e_shnum; block++) {
                sh_type = in.read(e_shoff + 40 * block + 4, 4);
                sh_offset = in.read(e_shoff + 40 * block + 16, 4);
                sh_size = in.read(e_shoff + 40 * block + 20, 4);

                if (sh_type == 2) {
                    for (int symb = 0; symb < sh_size / 16; symb++) {
                        st_name = in.read(sh_offset + 16 * symb, 4);
                        st_value = in.read(sh_offset + 16 * symb + 4, 4);
                        st_info = in.read(sh_offset + 16 * symb + 12, 1);

                        if ((st_info & 0b0000_1111) == 2) {
                            marks.put(st_value, title(st_name, setOfStrings));
                        }
                    }
                }
            }
            for (int section = 0; section < e_shnum; section++) {
                sh_name = in.read(e_shoff + 40 * section, 4);
                sh_addr = in.read(e_shoff + 40 * section + 12, 4);
                sh_offset = in.read(e_shoff + 40 * section + 16, 4);
                sh_size = in.read(e_shoff + 40 * section + 20, 4);
                if (title(sh_name, setOfStrings).equals(".text")) {
                    for (int c = 0; c < sh_size; c+=4) {
                        if (!marks.containsKey(sh_addr + c)) {
                            printMem(sh_addr + c, out);
                        } else {
                            printMem(sh_addr + c, marks.get(sh_addr + c), out);
                        }
                        Disassemble.printDisassemble(in.read(sh_offset + c, 4), out);
                    }
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            out.close();
        }
    }
}
