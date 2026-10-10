from pathlib import Path

import flatbuffers
import tflite


MODEL_PATH = Path(
    "autosense-ai/src/main/assets/efficientdet_lite0_int8.tflite"
)


def tensor_type_name(tensor_type):
    """Convert a TFLite tensor type enum into a readable name."""
    names = {
        tflite.TensorType.FLOAT32: "FLOAT32",
        tflite.TensorType.FLOAT16: "FLOAT16",
        tflite.TensorType.INT32: "INT32",
        tflite.TensorType.UINT8: "UINT8",
        tflite.TensorType.INT64: "INT64",
        tflite.TensorType.BOOL: "BOOL",
        tflite.TensorType.INT8: "INT8",
        tflite.TensorType.INT16: "INT16",
    }

    return names.get(tensor_type, f"UNKNOWN({tensor_type})")


def inspect_model(model_path: Path):
    if not model_path.is_file():
        raise FileNotFoundError(
            f"Model file not found: {model_path.resolve()}"
        )

    model_bytes = model_path.read_bytes()

    # The TFLite file contains a FlatBuffer Model object.
    model = tflite.Model.GetRootAsModel(
        model_bytes,
        0,
    )

    print("=" * 70)
    print("TFLite MODEL METADATA")
    print("=" * 70)
    print(f"File: {model_path.resolve()}")
    print(f"Size: {len(model_bytes):,} bytes")
    print(f"Schema version: {model.Version()}")

    subgraph_count = model.SubgraphsLength()
    print(f"Subgraphs: {subgraph_count}")

    if subgraph_count == 0:
        raise RuntimeError("Model contains no subgraphs")

    subgraph = model.Subgraphs(0)

    print("\nINPUT TENSORS")
    print("-" * 70)

    for index in range(subgraph.InputsLength()):
        tensor_index = subgraph.Inputs(index)
        tensor = subgraph.Tensors(tensor_index)

        print(f"\nInput index: {tensor_index}")
        print(f"Name: {tensor.Name().decode('utf-8')}")
        print(f"Shape: {tensor.ShapeAsNumpy().tolist()}")
        print(f"Type: {tensor_type_name(tensor.Type())}")

        quantization = tensor.Quantization()

        if quantization is not None:
            scales = (
                quantization.ScaleAsNumpy().tolist()
                if quantization.ScaleLength() > 0
                else []
            )

            zero_points = (
                quantization.ZeroPointAsNumpy().tolist()
                if quantization.ZeroPointLength() > 0
                else []
            )

            print(f"Quantization scales: {scales}")
            print(f"Quantization zero points: {zero_points}")
        else:
            print("Quantization: none")

    print("\nOUTPUT TENSORS")
    print("-" * 70)

    for index in range(subgraph.OutputsLength()):
        tensor_index = subgraph.Outputs(index)
        tensor = subgraph.Tensors(tensor_index)

        print(f"\nOutput index: {tensor_index}")
        print(f"Name: {tensor.Name().decode('utf-8')}")
        print(f"Shape: {tensor.ShapeAsNumpy().tolist()}")
        print(f"Type: {tensor_type_name(tensor.Type())}")

        quantization = tensor.Quantization()

        if quantization is not None:
            scales = (
                quantization.ScaleAsNumpy().tolist()
                if quantization.ScaleLength() > 0
                else []
            )

            zero_points = (
                quantization.ZeroPointAsNumpy().tolist()
                if quantization.ZeroPointLength() > 0
                else []
            )

            print(f"Quantization scales: {scales}")
            print(f"Quantization zero points: {zero_points}")
        else:
            print("Quantization: none")

    print("\n" + "=" * 70)
    print("INSPECTION COMPLETE")
    print("=" * 70)


if __name__ == "__main__":
    inspect_model(MODEL_PATH)