import configparser
import datetime
import logging
import os
import sys
import time
import traceback
from pathlib import Path
import json
import chromedriver_autoinstaller
from usecases.gtmaudit.operations.common import return_performance_logs, return_pgdatalayer, ReturnSeleniumDriver
from usecases.gtmaudit.operations.globalgtm import global_gtm
from usecases.gtmaudit.operations.googleanalytics import return_ga_id_status, return_analytics_obj
from usecases.gtmaudit.operations.googletagmanager import local_gtm
from usecases.gtmaudit.operations.pageviewpgdata import pageview_pgdatalayer
from usecases.gtmaudit.reports.report import generate_report
from usecases.gtmaudit.operations.sendemail import send_email
from typing import Dict
THREAD_NUM = 8
SLEEP_TIME = 7
print(os.getcwd())
config = configparser.RawConfigParser()
config.read(os.getcwd() + "\\usecases" + "\\gtmaudit\\" + "config.txt")
paths_config = dict(config.items('PATH'))
gtm_id = paths_config.get('gtm_account_ids')
client_secret_path = paths_config.get('client_secret')
log_file = paths_config.get('log_file_path')
BASE_PATH = Path(".")
if not Path(BASE_PATH / 'report').is_dir():
    os.mkdir(BASE_PATH / 'report')
    if not Path(BASE_PATH / 'report').is_dir():
        os.mkdir(BASE_PATH / 'report')
def GAGTMValidation(urls, consent_id_df, drivers):
    client_secret_file = open(client_secret_path)
    client_secret = json.load(client_secret_file)
    report_path = Path(BASE_PATH / 'report')
    mail_config: Dict = {}
    gtm_account_ids = gtm_id
    total_time = datetime.timedelta(0)
    time_stats = {}
    urls = [o.strip() for o in urls.split(",")]
    report = {}
    filename_suffix = "".join(
        ["GTM_AUDIT_", str(str(datetime.datetime.now())).replace(" ", "-").replace(".", "-").replace(":", "-"),
         '_report', '.json'])
    report_name = report_path / Path(filename_suffix)
    report_obj, gtm_obj = return_analytics_obj(client_secret)
    for index, url in enumerate(urls):
        sheet_name = "auditData"
        data = [["Items", "Expected_Result", "Implemented_Correctly", "Remarks"]]
        try:
            logs_status, logs = return_performance_logs(drivers[0], url, sleep_time)
            site_status, site_data = return_pgdatalayer(drivers[0], url)
            start = time.monotonic()
            if not logs_status and not site_status:
                report[sheet_name] = [
                    ['Items', 'Values'],
                    ['Network Logs', logs],
                    ['Site Data', site_data]
                ]
            else:
                gtm_status, gtm_value = global_gtm(logs)
                if gtm_status:
                    data(["Global GTM", "GTM-N94XXFB with 200 status code", "Yes",
                                 "GTM-N94XXFB status code is 200"])
                else:
                    data.append(["Global GTM", "GTM-N94XXFB with 200 status code", "No",
                                 f"GTM-N94XXFB status code is {gtm_value} "])
                decommissioned_variables = ['GhosteryAdChoices', 'GhosteryAdChoicesID', 'GhosteryOverlay',
                                            'GhosteryOverlayID', 'SiteGDPR']
                decommissioned_status = []
                site_data_keys = list(site_data.get('GTM').keys())
                for variable in decommissioned_variables:
                    if variable in site_data_keys:
                        decommissioned_variables.append(variable)
                if not decommissioned_status:
                    data.append(['Decommissioned Variables',
                                 f"Check {' ,'.join(decommissioned_variables)} is available PGDatalayer data", "Yes",
                                 "Variables not available"])
                else:
                    data.append(['Decommissioned Variables',
                                 f"Check {', '.join(decommissioned_variables)} is available PGDatalayer data", "No",
                                 f"{' ,'.join(decommissioned_status)} variables are available"])
                pageview_output, pgdatalayer_output = pageview_pgdatalayer(url, drivers, thread_num, sleep_time)
                if pageview_output[]:
                    data.append(["Page View",
                                 "1. Validate collect = pageview is set \n 2. Validate TID value matching with Pgdatalayer GTM value\n",
                                 "Yes", ])
                else:
                    data.append(["Page View",
                                 "1. Validate collect = pageview is set \n 2. Validate TID value matching with Pgdatalayer GTM value\n",
                                 "No", ",".join(pageview_output[1])])
                if pgdatalayer_output[]:
                    data.append(
                        ['PGDataLayer Validation', "PGDataLayer is available for all URL's in Sitemap.xml", "Yes", ""])
                else:
                    data.append(
                        ['PGDataLayer Validation', "PGDataLayer is available for all URL's in Sitemap.xml", "No",
                         ",".join(pgdatalayer_output[1])])
                cookie_consent = []
                cookie_consent_flag = True
                if site_data.get('GTM').get('ConsentOverlay') == 'One Trust':
                    cookie_consent.append("1. ConsentOverlay value is One Trust")
                else:
                    cookie_consent_flag = False
                    cookie_consent.append(f"1. ConsentOverlay value is {site_data.get('GTM').get('ConsentOverlay')}")

                if site_data.get('GTM').get('SitePrivacyProtection') in ['GDPR', 'CCPA', 'LGPD', 'AMA']:
                    cookie_consent.append(
                        f"2. SitePrivacyProtection value is {site_data.get('GTM').get('SitePrivacyProtection')}")
                else:
                    cookie_consent_flag = False
                    cookie_consent.append(
                        f"2. SitePrivacyProtection value is {site_data.get('GTM').get('SitePrivacyProtection')}")
                consent_overlay_id = consent_id_df
                if consent_overlay_id :
                    cookie_consent.append(
                        f"3. ConsentOverlayID: {consent_overlay_id}, ConsentOverlayID from mapped file:{url}")
                else:
                    cookie_consent_flag = False
                    cookie_consent.append(
                        f"3. ConsentOverlayID: {consent_overlay_id}, ConsentOverlayID from mapped file:{url }, [NOTE: if url not available in mapping list value, default value is 'None']")
                if cookie_consent_flag:
                    data.append(['Cookie Consent Validation',
                                 'Check ConsentOverlay value is OneTrust, SitePrivacyProtection values is in [GDPR, CCPA, LGPD, AMA] list, check ConsentOverlayID matches ID from mapping file',
                                 "Yes", ".\n\n ".join(cookie_consent)])
                else:
                    data.append(['Cookie Consent Validation',
                                 '1. Check ConsentOverlay value is OneTrust\n 2.Check SitePrivacyProtection values is in [GDPR, CCPA, LGPD, AMA] list\n 3. check ConsentOverlayID matches ID from mapping file\n',
                                 "No", ".\n\n".join(cookie_consent)])
                if len(data) > 1:
                    report[sheet_name] = data
                end = time.monotonic()
                time_diff = datetime.timedelta(seconds=end - start)
                total_time += time_diff
                time_stats[url] = str(time_diff)
        except Exception as e:
            print(f"Exception occured (gtmmain) : {e}")
    report_fp = generate_report(report_name, report)
    print(f'TOTAL TIME TAKEN FOR PROCESSING {len(urls)} URLS: {str(total_time)}')
    print(f'\n\n INDIVIDUAL PROCESSING TIME STATS: {time_stats}')
    mail_flag = int(mail_config.get('send_mail', "0").strip())
    if mail_flag:
        to = mail_config.get('to', "")
        if to:
            to = ";".join([o.strip() for o in to.split(',')])
            cc = ";".join([o.strip() for o in mail_config.get('cc', "").split(',')])
            bcc = ";".join([o.strip() for o in mail_config.get('bcc', "").split(',')])
            subject = f"GTM AUDIT AUTOMATION REPORT | {Path(report_name).name}"
            print(to, cc, bcc, subject, report_name, Path(report_name).name)
            send_email(to, cc, bcc, subject, str(report_name))
        else:
            print("MAIL CANNOT BE SENT NULL TO-ADDRESS")
    else:
        print('MAIL FLAG IS UNSET IN CONFIG FILE')
    print(report_fp, type(report_fp))
    return True, report_fp