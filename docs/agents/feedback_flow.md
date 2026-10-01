# Feedback flow

Use this runbook when moving ratings from a connected device into the project record.

## Data ownership

- `docs/recorded_ratings.json` is a transient device-output snapshot. Do not treat it as historical storage.
- `docs/master_ratings.json` is the cumulative source of truth. Copy or merge every retained rating from the transient file into this file before replacing or clearing the transient output.
- `cyberpunkandroid/src/main/assets/test_doe_*.json` contains the design-of-experiments inputs used by the feedback fixture.

## Workflow

1. Capture or refresh the connected-device output in `docs/recorded_ratings.json`.
2. Merge the ratings to retain into `docs/master_ratings.json`.
3. Verify the merged entries in `docs/master_ratings.json` before replacing or clearing the transient file.
4. Update the matching design-of-experiments asset only when the next device run needs different inputs.

For a direct device pull, `python py_scripts/pull_and_merge.py` merges the device rating files into `docs/master_ratings.json` and removes those files from the device after a successful pull.
