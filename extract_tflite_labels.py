from pathlib import Path
import zipfile

MODEL_PATH = Path(
    "autosense-ai/src/main/assets/efficientdet_lite0_int8.tflite"
)

OUTPUT_PATH = Path("efficientdet_lite0_labels.txt")

print("=" * 70)
print("EXTRACTING EFFICIENTDET LITE0 LABELS")
print("=" * 70)

with zipfile.ZipFile(MODEL_PATH, "r") as archive:
    print("\nEmbedded files:")
    for name in archive.namelist():
        print(f"  {name}")

    if "labels.txt" not in archive.namelist():
        raise FileNotFoundError(
            "labels.txt was not found in the model archive."
        )

    labels_content = archive.read("labels.txt").decode("utf-8")

OUTPUT_PATH.write_text(labels_content, encoding="utf-8")

labels = labels_content.splitlines()

print(f"\nExtracted to: {OUTPUT_PATH}")
print(f"Number of labels: {len(labels)}")

print("\nLabels with their line numbers:")
for index, label in enumerate(labels):
    print(f"{index:3}: {label}")

print("\nExtraction complete.")