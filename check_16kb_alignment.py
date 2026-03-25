#!/usr/bin/env python3
import struct
import sys

def check_elf_alignment(filepath):
    """
    Verifica si un archivo ELF está alineado a 16 KB (0x4000 bytes)
    para compatibilidad con Android 15+ (16 KB page size)
    """
    with open(filepath, 'rb') as f:
        # Leer el header ELF
        f.seek(0)
        elf_header = f.read(64)

        # Verificar magic number
        if elf_header[:4] != b'\x7fELF':
            print(f"Error: {filepath} no es un archivo ELF válido")
            return False

        # Determinar si es 32-bit (1) o 64-bit (2)
        ei_class = elf_header[4]

        # Determinar endianness: 1 = little-endian, 2 = big-endian
        ei_data = elf_header[5]

        if ei_data == 1:
            endian = '<'  # little-endian
        elif ei_data == 2:
            endian = '>'  # big-endian
        else:
            print(f"Error: Endianness inválido en {filepath}")
            return False

        # Leer e_phoff (offset a program headers)
        if ei_class == 1:  # 32-bit
            e_phoff_offset = 28
            e_phnum_offset = 44
            e_phentsize_offset = 42
        else:  # 64-bit
            e_phoff_offset = 32
            e_phnum_offset = 56
            e_phentsize_offset = 54

        # Leer los offsets necesarios
        f.seek(e_phoff_offset)
        if ei_class == 1:  # 32-bit
            e_phoff = struct.unpack(endian + 'I', f.read(4))[0]
        else:  # 64-bit
            e_phoff = struct.unpack(endian + 'Q', f.read(8))[0]

        f.seek(e_phentsize_offset)
        e_phentsize = struct.unpack(endian + 'H', f.read(2))[0]

        f.seek(e_phnum_offset)
        e_phnum = struct.unpack(endian + 'H', f.read(2))[0]

        print(f"\nAnalizando: {filepath}")
        print(f"  Tipo: {'64-bit' if ei_class == 2 else '32-bit'}")
        print(f"  Endianness: {'Little-endian' if ei_data == 1 else 'Big-endian'}")
        print(f"  Program headers: {e_phnum}")
        print(f"  Program header size: {e_phentsize}")
        print(f"  Program header offset: 0x{e_phoff:x}")

        # Verificar alineación de LOAD segments
        all_aligned = True
        for i in range(e_phnum):
            f.seek(e_phoff + i * e_phentsize)
            p_type = struct.unpack(endian + 'I', f.read(4))[0]

            if p_type == 1:  # PT_LOAD
                if ei_class == 2:  # 64-bit
                    # En 64-bit: p_flags (4 bytes), p_offset (8 bytes)
                    f.seek(e_phoff + i * e_phentsize + 4)
                    p_flags = struct.unpack(endian + 'I', f.read(4))[0]
                    p_offset = struct.unpack(endian + 'Q', f.read(8))[0]
                    p_vaddr = struct.unpack(endian + 'Q', f.read(8))[0]
                    p_align = struct.unpack(endian + 'Q', f.read(8))[0]
                else:  # 32-bit
                    f.seek(e_phoff + i * e_phentsize + 4)
                    p_offset = struct.unpack(endian + 'I', f.read(4))[0]
                    p_vaddr = struct.unpack(endian + 'I', f.read(4))[0]
                    p_filesz = struct.unpack(endian + 'I', f.read(4))[0]
                    p_memsz = struct.unpack(endian + 'I', f.read(4))[0]
                    p_flags = struct.unpack(endian + 'I', f.read(4))[0]
                    p_align = struct.unpack(endian + 'I', f.read(4))[0]

                is_aligned = (p_offset % 0x4000) == 0
                alignment_status = "✓ 16 KB ALINEADO" if is_aligned else "✗ NO ALINEADO"
                print(f"  LOAD segment {i}:")
                print(f"    Offset: 0x{p_offset:x} - {alignment_status}")
                print(f"    Virtual Address: 0x{p_vaddr:x}")
                print(f"    Alineación requerida: {p_align} bytes (0x{p_align:x})")

                if not is_aligned:
                    all_aligned = False

        return all_aligned

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python check_16kb_alignment.py <archivo.so>")
        sys.exit(1)

    filepath = sys.argv[1]
    result = check_elf_alignment(filepath)

    if result:
        print(f"\n✓ {filepath} está correctamente alineado a 16 KB")
        sys.exit(0)
    else:
        print(f"\n✗ {filepath} NO está alineado a 16 KB")
        sys.exit(1)

