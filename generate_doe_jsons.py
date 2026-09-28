import json

tests = [
    {"test_name": "doe_bounce", "icon_size_dp": 64, "values": list(range(8))},
    {"test_name": "doe_float", "icon_size_dp": 64, "values": list(range(8))},
    {"test_name": "doe_blur", "icon_size_dp": 64, "values": list(range(9))},
    {"test_name": "doe_stripes", "icon_size_dp": 64, "values": list(range(9))},
    {"test_name": "doe_glowborder", "icon_size_dp": 64, "values": list(range(8))},
    {"test_name": "doe_glow", "icon_size_dp": 64, "values": list(range(8))},
    {"test_name": "doe_datastream", "icon_size_dp": 64, "values": list(range(4))}
]

for t in tests:
    with open(f"cyberpunkandroid/src/main/assets/test_{t['test_name']}.json", 'w') as f:
        json.dump(t, f, indent=4)
