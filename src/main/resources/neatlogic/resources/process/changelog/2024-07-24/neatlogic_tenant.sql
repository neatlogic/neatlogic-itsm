update process_workcenter set is_show_total = 0 where uuid != 'processingOfMineProcessTask';

update process_workcenter set is_show_total = 1 where uuid = 'processingOfMineProcessTask';