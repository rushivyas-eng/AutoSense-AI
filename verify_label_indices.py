from pathlib import Path

LABELS_PATH = Path("efficientdet_lite0_labels.txt")

labels = LABELS_PATH.read_text(encoding="utf-8").splitlines()

print(f"Total labels: {len(labels)}")
print(f"Expected model classification channels: 90")
print()

print("First 15 entries:")
for index, label in enumerate(labels[:15]):
    print(f"Line {index + 1:2} | Zero-based index {index:2} | {label}")

print("\nImportant classes:")
target_labels = {
    "person",
    "bicycle",
    "car",
    "motorcycle",
    "bus",
    "truck",
    "traffic light",
    "stop sign",
}

for index, label in enumerate(labels):
    if label in target_labels:
        print(
            f"Line {index + 1:2} | "
            f"Zero-based index {index:2} | "
            f"{label}"
        )

print("\nPlaceholder positions:")
for index, label in enumerate(labels):
    if label.strip() == "???":
        print(f"Line {index + 1:2} | Zero-based index {index:2}")

print("\nVerification complete.")