"""Merge the per-run CSV files of each experiment into one CSV per experiment.

Each input file starts with "KEY:value" parameter lines, followed by a regular
CSV table. The parameters, plus the algorithm and level set taken from the file
name (e.g. "astar-krys-SS-1-TTFW-0.2.csv"), are added as columns to every row.

Output: data/common.csv and data/elective.csv
"""
import csv
from pathlib import Path

DATA_DIR = Path(__file__).parent / "data"
EXPERIMENTS = {"commonTask": "common.csv", "electiveTask": "elective.csv"}


def read_run(path):
    params, table_lines = {}, []
    with path.open(newline="") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            key, sep, value = line.partition(":")
            if sep and not table_lines and "," not in line:
                params[key] = value
            else:
                table_lines.append(line)
    algorithm, level_set = path.stem.split("-")[:2]
    meta = {"algorithm": algorithm, "level set": level_set, **params}
    return meta, list(csv.DictReader(table_lines))


def merge(folder, out_name):
    rows, columns = [], []
    for path in sorted((DATA_DIR / folder).glob("*.csv")):
        meta, table = read_run(path)
        for row in table:
            merged = {**meta, **row}
            rows.append(merged)
            columns += [c for c in merged if c not in columns]
    out_path = DATA_DIR / out_name
    with out_path.open("w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)
    print(f"{out_path}: {len(rows)} rows")


if __name__ == "__main__":
    for folder, out_name in EXPERIMENTS.items():
        merge(folder, out_name)
