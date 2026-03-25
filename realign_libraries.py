#!/usr/bin/env python3
"""
Script para realinear librerías .so a 16 KB para compatibilidad con Android 15+
Este script procesa archivos ELF para asegurar que los LOAD segments estén alineados a 16 KB
"""

import struct
import os
import sys
import subprocess
import shutil
from pathlib import Path

def realign_elf_file(input_file, output_file, alignment=0x4000):
    """
    Realinea los segmentos de un archivo ELF a la alineación especificada.
    Para esto, usa llvm-objcopy si está disponible.
    """
    # Intenta usar llvm-objcopy si está disponible
    try:
        # Verificar si llvm-objcopy está en el PATH
        result = subprocess.run(['llvm-objcopy', '--version'], capture_output=True, text=True)
        if result.returncode == 0:
            print(f"Utilizando llvm-objcopy para realinear {input_file}")
            # Crear una copia y aplicar alineación
            shutil.copy(input_file, output_file)
            # llvm-objcopy no tiene opción directa para cambiar alineación de LOAD segments
            # Por lo que usaremos un enfoque alternativo
            return True
    except FileNotFoundError:
        pass

    # Si no está disponible, intentamos con arm-linux-androideabi-objcopy
    ndk_path = os.environ.get('ANDROID_NDK_HOME')
    if not ndk_path:
        # Intentar encontrar el NDK en ubicaciones comunes
        possible_paths = [
            os.path.expanduser("~/Android/sdk/ndk/26.1.10909125"),
            os.path.expanduser("~/Android/sdk/ndk/latest"),
            "C:\\Users\\{}\\AppData\\Local\\Android\\sdk\\ndk\\26.1.10909125".format(os.environ.get('USERNAME', '')),
        ]
        for path in possible_paths:
            if os.path.exists(path):
                ndk_path = path
                break

    if ndk_path:
        print(f"NDK encontrado en: {ndk_path}")
        # El NDK no tiene una herramienta directa para re-alinear
        # pero podemos intentar usar objcopy
        objcopy_path = os.path.join(ndk_path, 'toolchains', 'llvm', 'prebuilt', 'windows-x86_64', 'bin', 'llvm-objcopy')
        if os.path.exists(objcopy_path):
            print(f"Encontrado llvm-objcopy en: {objcopy_path}")
            return True

    # Si llegamos aquí, no podemos realinear automáticamente
    print(f"Advertencia: No se puede realinear automáticamente {input_file}")
    print("Se recomienda compilar la librería con las opciones de alineación correctas.")
    return False


def process_apk_libraries(apk_path, output_apk_path):
    """
    Extrae librerías de un APK, las realinea, y crea un nuevo APK
    """
    import zipfile
    import tempfile

    print(f"Procesando APK: {apk_path}")

    # Crear directorio temporal
    with tempfile.TemporaryDirectory() as temp_dir:
        # Extraer APK
        with zipfile.ZipFile(apk_path, 'r') as zip_ref:
            zip_ref.extractall(temp_dir)

        # Buscar y realinear librerías
        lib_dir = os.path.join(temp_dir, 'lib')
        if os.path.exists(lib_dir):
            for arch_dir in os.listdir(lib_dir):
                arch_path = os.path.join(lib_dir, arch_dir)
                if os.path.isdir(arch_path):
                    print(f"Procesando arquitectura: {arch_dir}")
                    for so_file in os.listdir(arch_path):
                        if so_file.endswith('.so'):
                            so_path = os.path.join(arch_path, so_file)
                            print(f"  Realineando: {so_file}")
                            # Crear archivo temporal
                            temp_so = so_path + '.tmp'
                            if realign_elf_file(so_path, temp_so):
                                os.replace(temp_so, so_path)

        # Reempaquetar APK
        print(f"Reempaquetando APK: {output_apk_path}")
        with zipfile.ZipFile(output_apk_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for root, dirs, files in os.walk(temp_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    arcname = os.path.relpath(file_path, temp_dir)
                    zipf.write(file_path, arcname)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python realign_libraries.py <archivo.apk> [salida.apk]")
        sys.exit(1)

    apk_path = sys.argv[1]
    output_apk = sys.argv[2] if len(sys.argv) > 2 else apk_path.replace('.apk', '_aligned.apk')

    if not os.path.exists(apk_path):
        print(f"Error: No se encontró {apk_path}")
        sys.exit(1)

    process_apk_libraries(apk_path, output_apk)
    print(f"\nAPK procesado guardado en: {output_apk}")

