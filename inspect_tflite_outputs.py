from pathlib import Path
import tflite

MODEL_PATH = Path(
    "autosense-ai/src/main/assets/efficientdet_lite0_int8.tflite"
)

model_bytes = MODEL_PATH.read_bytes()
model = tflite.Model.GetRootAsModel(model_bytes, 0)

subgraph = model.Subgraphs(0)

TENSOR_NAMES = {
    "serving_default_images:0",
    "StatefulPartitionedCall:1",
    "StatefulPartitionedCall:0",
}

def tensor_type_name(value):
    names = {
        0: "FLOAT32",
        1: "FLOAT16",
        2: "INT32",
        3: "UINT8",
        4: "INT64",
        5: "STRING",
        6: "BOOL",
        7: "INT16",
        9: "INT8",
    }
    return names.get(value, f"UNKNOWN({value})")


print("=" * 72)
print("MODEL TENSOR DETAILS")
print("=" * 72)

for index in range(subgraph.TensorsLength()):
    tensor = subgraph.Tensors(index)
    name = tensor.Name().decode("utf-8")

    if name not in TENSOR_NAMES:
        continue

    quantization = tensor.Quantization()

    print(f"\nTensor index: {index}")
    print(f"Name: {name}")
    print(f"Shape: {tensor.ShapeAsNumpy().tolist()}")
    print(f"Type: {tensor_type_name(tensor.Type())}")

    if quantization is None:
        print("Quantization: none")
    else:
        def to_python_list(value):
            if hasattr(value, "tolist"):
                result = value.tolist()
                return result if isinstance(result, list) else [result]

            if isinstance(value, (list, tuple)):
                return list(value)

            return [value]


        print(
            "Quantization scales:",
            to_python_list(quantization.ScaleAsNumpy()),
        )
        print(
            "Quantization zero points:",
            to_python_list(quantization.ZeroPointAsNumpy()),
        )

print("\n" + "=" * 72)
print("OUTPUT ACTIVATION TRACE")
print("=" * 72)

target_indices = {592, 593, 595, 596}

for operator_index in range(subgraph.OperatorsLength()):
    operator = subgraph.Operators(operator_index)

    inputs = [
        int(value)
        for value in operator.InputsAsNumpy().tolist()
        if int(value) >= 0
    ]

    outputs = [
        int(value)
        for value in operator.OutputsAsNumpy().tolist()
        if int(value) >= 0
    ]

    if not (
        target_indices.intersection(inputs)
        or target_indices.intersection(outputs)
    ):
        continue

    opcode = model.OperatorCodes(operator.OpcodeIndex())
    builtin_code = opcode.BuiltinCode()

    custom_code = opcode.CustomCode()
    if custom_code:
        operation_name = custom_code.decode("utf-8")
    else:
        operation_name = f"BUILTIN({builtin_code})"

    print(f"\nOperator index: {operator_index}")
    print(f"Operation: {operation_name}")
    print(f"Inputs: {inputs}")
    print(f"Outputs: {outputs}")

    for tensor_index in inputs + outputs:
        tensor = subgraph.Tensors(tensor_index)
        name = tensor.Name().decode("utf-8")

        print(
            f"  Tensor {tensor_index}: "
            f"name={name}, "
            f"shape={tensor.ShapeAsNumpy().tolist()}, "
            f"type={tensor_type_name(tensor.Type())}"
        )

print("\nInspection complete.")