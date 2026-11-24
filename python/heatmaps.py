"""Heatmaps of experiment results over the two varied parameters.

Reads data/common.csv and data/elective.csv (made by merge_csv.py) and writes
one PNG per experiment and metric to plots/. Each cell is the mean over all
levels (of all level sets) for that parameter combination.
"""
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from matplotlib.colors import LogNorm

ROOT = Path(__file__).parent
OUT_DIR = ROOT / "plots"

# experiment name -> (x param, y param)
EXPERIMENTS = {"common": ("TTFW", "SS"), "elective": ("DFPMP", "TTT")}

# metric column -> (title, colormap, log scale, number format)
METRICS = {
    "win rate": ("Win rate", "RdYlGn", False, ".2f"),
    "run time": ("Mean run time (ms)", "viridis", True, ".0f"),
    "nodes evaluated": ("Mean nodes evaluated", "viridis", True, ".2g"),
}


def plot(df, experiment, x, y, metric):
    title, cmap, log, fmt = METRICS[metric]
    table = (df.pivot_table(index=y, columns=x, values=metric, aggfunc="mean")
             .sort_index(ascending=False))

    norm, vmin, vmax = None, None, None
    if log:
        norm = LogNorm(max(table.min().min(), 1), table.max().max())
    elif metric == "win rate":
        vmin, vmax = 0, 1

    fig, ax = plt.subplots(figsize=(8, 6.5), constrained_layout=True)
    sns.heatmap(table, ax=ax, cmap=cmap, norm=norm, vmin=vmin, vmax=vmax,
                annot=True, fmt=fmt, annot_kws={"size": 8},
                cbar_kws={"label": title})
    ax.set_title(f"{experiment}: {title} (all levels)", fontsize=13)

    out = OUT_DIR / f"{experiment}_{metric.replace(' ', '_')}.png"
    fig.savefig(out, dpi=150)
    plt.close(fig)
    print(out)


def main():
    OUT_DIR.mkdir(exist_ok=True)
    for experiment, (x, y) in EXPERIMENTS.items():
        df = pd.read_csv(ROOT / "data" / f"{experiment}.csv")
        df["win rate"] = df["win/fail"].astype(float)
        for metric in METRICS:
            plot(df, experiment, x, y, metric)


if __name__ == "__main__":
    main()
