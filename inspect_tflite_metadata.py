from pathlib import Path
import zipfile
import tflite

MODEL_PATH = Path(
    "autosense-ai/src/main/assets/efficientdet_lite0_int8.tflite"
)

model_bytes = MODEL_PATH.read_bytes()
model = tflite.Model.GetRootAsModel(model_bytes, 0)

print("=" * 70)
print("EFFICIENTDET LITE0 MODEL METADATA")
print("=" * 70)

print(f"Model: {MODEL_PATH}")
print(f"Size: {len(model_bytes):,} bytes")
print(f"Schema version: {model.Version()}")

metadata_count = model.MetadataLength()
print(f"\nMetadata entries: {metadata_count}")

for index in range(metadata_count):
    metadata = model.Metadata(index)
    name = metadata.Name().decode("utf-8")
    buffer_index = metadata.Buffer()

    print(f"\nMetadata[{index}]")
    print(f"Name: {name}")
    print(f"Buffer index: {buffer_index}")

    if buffer_index < model.BuffersLength():
        data = model.Buffers(buffer_index).DataAsNumpy()

        if data is not None:
            print(f"Metadata buffer size: {len(data)} bytes")

print("\n" + "=" * 70)
print("ASSOCIATED ZIP FILES")
print("=" * 70)

try:
    with zipfile.ZipFile(MODEL_PATH) as archive:
        names = archive.namelist()

        if not names:
            print("No ZIP entries found.")
        else:
            for name in names:
                info = archive.getinfo(name)
                print(f"{name} ({info.file_size:,} bytes)")

except zipfile.BadZipFile:
    print("Model does not contain a readable appended ZIP archive.")

print("\nInspection complete.")