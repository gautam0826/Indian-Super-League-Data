import json
from typing import Any, Dict, List

import os
import pandas as pd
from prefect import task, Flow, Parameter
from prefect.engine.executors import DaskExecutor
import requests
import unidecode

from isl_analytics.util import file_utils


schedule_url = "https://www.indiansuperleague.com/feeds-schedule/?methodtype=3&client=8&sport=2&league=0&timezone=0530&language=en&gamestate=3"
game_base_url = (
    "https://www.indiansuperleague.com/sifeeds/repo/football/live/india_sl/json/%s.json"
)

timeout = 15
overwrite = False


def download_data(url, outfile_path):
    r = requests.get(url, timeout=timeout)
    outfile = open(outfile_path, "w")
    outfile.write(unidecode.unidecode(r.text))
    outfile.close()
    return r.status_code


@task
def download_match_data(game_id):
    url = game_base_url % (game_id)
    file = "%s.json" % (game_id)
    outfile_path = file_utils.get_raw_data_filepath([file])
    if not overwrite and os.path.exists(outfile_path):
        return (200, game_id)
    return (download_data(url, outfile_path), game_id)


@task
def download_schedule_data():
    return download_data(
        schedule_url, file_utils.get_raw_data_filepath(["schedule.json"])
    )


@task
def get_game_id_list():
    return [
        game["game_id"]
        for game in json.load(
            open(file_utils.get_raw_data_filepath(["schedule.json"]), "r")
        )["matches"]
    ]

import pandas as pd
from pandas.io.json import json_normalize

def save_schedule():
    schedule = json.load(open(file_utils.get_raw_data_filepath(["schedule.json"]), "r"))
    df_schedule = json_normalize(schedule["matches"])
    df_schedule.to_csv('a.csv')

#save_schedule()

if __name__ == "__main__":
	with Flow("scrape_isl") as flow:
		download_schedule_data()
		game_ids = get_game_id_list(upstream_tasks=[download_schedule_data])
		download_match_data.map(game_ids)

	executor = DaskExecutor(local_processes=True)

	flow.run(executor=executor)