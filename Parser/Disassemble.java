package evm;

import java.io.PrintStream;

public class Disassemble {
    private static void printVariant0(String a, String b, String c, PrintStream out) {
        out.printf("%s\t%s, %s\n", a, b, c);
    }

    private static void printVariant1(String a, String b, String c, String d, PrintStream out) {
        out.printf("%s\t%s, %s, %s\n", a, b, c, d);
    }

    private static void printVariant2(String a, String b, String c, String d, PrintStream out) {
        out.printf("%s\t%s, %s(%s)\n", a, b, c, d);
    }

    private static void printVariant3(String a, PrintStream out) {
        out.printf("%s\n", a);
    }

    private static String register(int x) {
        if (x == 0) {
            return "zero";
        }
        if (x == 1) {
            return "ra";
        }
        if (x == 2) {
            return "sp";
        }
        if (x == 3) {
            return "gp";
        }
        if (x == 4) {
            return "tp";
        }
        if (x == 5) {
            return "t0";
        }
        if (6 <= x && x <= 7) {
            return "t" + (x - 5);
        }
        if (x == 8) {
            return "s0";
        }
        if (x == 9) {
            return "s1";
        }
        if (10 <= x && x <= 11) {
            return "a" + (x - 10);
        }
        if (12 <= x && x <= 17) {
            return "a" + (x - 10);
        }
        if (18 <= x && x <= 27) {
            return "s" + (x - 16);
        }
        if (28 <= x && x <= 31) {
            return "t" + (x - 25);
        }
        return null;
    }

    private static String str(int x) {
        return Integer.toString(x);
    }

    private static String unsigned_str(int x) {
        return Integer.toUnsignedString(x);
    }

    public static void printDisassemble(int instr, PrintStream out) {
        int funct7 = (instr & 0b1111111_00000_00000_000_00000_0000000) >>> 25;
        int rs2 = (instr & 0b0000000_11111_00000_000_00000_0000000) >>> 20;
        int rs1 = (instr & 0b0000000_00000_11111_000_00000_0000000) >>> 15;
        int funct3 = (instr & 0b0000000_00000_00000_111_00000_0000000) >>> 12;
        int rd = (instr & 0b0000000_00000_00000_000_11111_0000000) >>> 7;
        int opcode = (instr & 0b0000000_00000_00000_000_00000_1111111);
        int imm;
        int offset;
        int shamt;
        int pred;
        int succ;
        int zimm;
        if (opcode == 0b0110111) {
            printVariant0("lui", register(rd), unsigned_str((instr >>> 12) << 12), out);
        } else if (opcode == 0b0010111) {
            printVariant0("auipc", register(rd), unsigned_str((instr >>> 12) << 12), out);
        } else if (opcode == 0b1101111) {
            offset = 0;
            imm = instr >>> 12;
            offset |= (imm & 0b1_0000000000_0_00000000) << 1;
            offset |= (imm & 0b0_1111111111_0_00000000) >>> 8;
            offset |= (imm & 0b0_0000000000_1_00000000) << 3;
            offset |= (imm & 0b0_0000000000_0_11111111) << 12;
            offset -= (offset & (1 << 20)) << 1;
            printVariant0("jal", register(rd), str(offset), out);
        } else if (opcode == 0b1100111) {
            printVariant1("jalr", register(rd), register(rs1), str(instr >>> 20), out);
        } else if (opcode == 0b1100011) {
            offset = 0;
            imm = funct7;
            offset |= (imm & 0b1_000000) << 6;
            offset |= (imm & 0b0_111111) << 5;
            imm = rd;
            offset |= (imm & 0b1111_0);
            offset |= (imm & 0b0000_1) << 11;
            if (funct3 == 0b000) {
                printVariant1("beq", register(rs1), register(rs2), str(offset), out);
            } else if (funct3 == 0b001) {
                printVariant1("bne", register(rs1), register(rs2), str(offset), out);
            } else if (funct3 == 0b100) {
                printVariant1("blt", register(rs1), register(rs2), str(offset), out);
            } else if (funct3 == 0b101) {
                printVariant1("bge", register(rs1), register(rs2), str(offset), out);
            } else if (funct3 == 0b110) {
                printVariant1("bltu", register(rs1), register(rs2), str(offset), out);
            } else if (funct3 == 0b111) {
                printVariant1("bgeu", register(rs1), register(rs2), str(offset), out);
            } else {
                out.println("unknown instruction");
            }
        } else if (opcode == 0b0000011) {
            offset = instr >>> 20;
            if (funct3 == 0b000) {
                printVariant2("lb", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b001) {
                printVariant2("lh", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b010) {
                printVariant2("lw", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b100) {
                printVariant2("lbu", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b101) {
                printVariant2("lhu", register(rd), str(offset), register(rs1), out);
            } else {
                out.println("unknown instruction");
            }
        } else if (opcode == 0b0100011) {
            offset = 0;
            imm = funct7;
            offset |= imm << 5;
            imm = rd;
            offset |= imm;
            if (funct3 == 0b000) {
                printVariant2("sb", register(rs2), str(offset), register(rs1), out);
            } else if (funct3 == 0b001) {
                printVariant2("sh", register(rs2), str(offset), register(rs1), out);
            } else if (funct3 == 0b010) {
                printVariant2("sw", register(rs2), str(offset), register(rs1), out);
            } else {
                out.println("unknown instruction");
            }
        } else if (opcode == 0b0010011) {
            imm = instr >>> 20;
            imm -= (imm & 0b1000_00000000) << 1;
            shamt = rs2;
            if (funct3 == 0b000) {
                printVariant1("addi", register(rd), register(rs1), str(imm), out);
            } else if (funct3 == 0b010) {
                printVariant1("slti", register(rd), register(rs1), str(imm), out);
            } else if (funct3 == 0b011) {
                printVariant1("sltiu", register(rd), register(rs1), str(imm >= 0 ? imm : imm + (1 << 12)), out);
            } else if (funct3 == 0b100) {
                printVariant1("xori", register(rd), register(rs1), str(imm), out);
            } else if (funct3 == 0b110) {
                printVariant1("ori", register(rd), register(rs1), str(imm), out);
            } else if (funct3 == 0b111) {
                printVariant1("andi", register(rd), register(rs1), str(imm), out);
            } else if (funct3 == 0b001) {
                printVariant1("slli", register(rd), register(rs1), str(shamt), out);
            } else {
                if (funct7 == 0b0000000) {
                    printVariant1("srli", register(rd), register(rs1), str(shamt), out);
                } else if (funct7 == 0b0100000) {
                    printVariant1("srai", register(rd), register(rs1), str(shamt), out);
                } else {
                    out.println("unknown instruction");
                }
            }
        } else if (opcode == 0b0110011) {
            if (funct3 == 0b000) {
                if (funct7 == 0b0000000) {
                    printVariant1("add", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0100000) {
                    printVariant1("sub", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("mul", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b001) {
                if (funct7 == 0b0000000) {
                    printVariant1("sll", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("mulh", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b010) {
                if (funct7 == 0b0000000) {
                    printVariant1("slt", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("mulhsu", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b011) {
                if (funct7 == 0b0000000) {
                    printVariant1("sltu", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("mulhu", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b100) {
                if (funct7 == 0b0000000) {
                    printVariant1("xor", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("div", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b101) {
                if (funct7 == 0b0000000) {
                    printVariant1("srl", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0100000) {
                    printVariant1("sra", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("divu", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b110) {
                if (funct7 == 0b0000000) {
                    printVariant1("or", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("rem", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            } else {
                if (funct7 == 0b0000000) {
                    printVariant1("and", register(rd), register(rs1), register(rs2), out);
                } else if (funct7 == 0b0000001) {
                    printVariant1("remu", register(rd), register(rs1), register(rs2), out);
                } else {
                    out.println("unknown instruction");
                }
            }
        } else if (opcode == 0b0001111) {
            pred = (instr & 0b00001111_00000000_00000000_00000000) >>> 24;
            succ = (instr & 0b00000000_11110000_00000000_00000000) >>> 20;
            if (funct3 == 0b000) {
                printVariant0("fence", str(pred), str(succ), out);
            } else if (funct3 == 0b001) {
                printVariant3("fence.i", out);
            } else {
                out.println("unknown instruction");
            }
        } else if (opcode == 0b1110011) {
            funct7 = instr >>> 20;
            offset = instr >>> 20;
            zimm = rs1;
            if (funct3 == 0b000) {
                if (funct7 == 0b000000000000) {
                    printVariant3("ecall", out);
                } else if (funct7 == 0b000000000001) {
                    printVariant3("ebreak", out);
                } else {
                    out.println("unknown instruction");
                }
            } else if (funct3 == 0b001) {
                printVariant1("csrrw", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b010) {
                printVariant1("csrrs", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b011) {
                printVariant1("csrrc", register(rd), str(offset), register(rs1), out);
            } else if (funct3 == 0b101) {
                printVariant1("csrrwi", register(rd), str(offset), str(zimm), out);
            } else if (funct3 == 0b110) {
                printVariant1("csrrsi", register(rd), str(offset), str(zimm), out);
            } else if (funct3 == 0b111) {
                printVariant1("csrrci", register(rd), str(offset), str(zimm), out);
            } else {
                out.println("unknown instruction");
            }
        } else {
            out.println("unknown instruction");
        }
    }
}
