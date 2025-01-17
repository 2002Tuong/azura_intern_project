import xml.etree.ElementTree as ET

xml_data = """
<strings>
    <string name="blood_pressure">Blood Pressure</string>
    <string name="heart_rate">Heart Rate</string>
    <string name="blood_sugar">Blood Sugar</string>
    <string name="weight_bmi">Weight &amp; BMI></string>
    <string name="cholesterol">Cholesterol</string>
    <string name="step_counter">Step Counter</string>
    <string name="water_reminder">Water Reminder</string>
    <string name="stress_level">Stress Level</string>
    <string name="measure">Measure</string>
    <string name="start_measure">Start Measure</string>
    <string name="progress">Progress</string>
    <string name="place_your_finger_on_camera">Place your finger on camera. When the finder turn red, you\'re doing it right</string>
    <string name="measuring">Measuring...</string>
    <string name="add_measure">Add Measure</string>
    <string name="measure_now">Measure Now</string>
    <string name="measure_your_heart_rate_simply_by_using_your_finger">Measure your heart rate simply by using your finger</string>
    <string name="add_manually">Add Manually</string>
    <string name="manually_add_a_heart_rate">Manually add a heart rate reading you\'ve already got</string>
    <string name="history">History</string>
    <string name="check_the_record_of_your_measurements">Check the record of your measurements</string>
    <string name="trends">Trends</string>
    <string name="get_detailed_analysis">Get detailed analysis of short, medium and long term trends</string>
    <string name="statistics">Statistics</string>
    <string name="view_the_detailed">View the detailed range and distribution of your measurement</string>
    <string name="set_alarms">Set Alarms</string>
    <string name="schedule_smart_alarms">Schedule smart alarms for health so you won\'t miss any regular measurement</string>
    <string name="bpm">BPM</string>
    <string name="slow">Slow</string>
    <string name="fast">Fast</string>
    <string name="your_heart_rate_remains_normal">Great! Your heart rate remains in the normal range</string>
    <string name="add">Add</string>
    <string name="your_heart_rate_too_low">Cautious! Your heart rate is too low.</string>
    <string name="your_heart_rate_high">Cautious! Your heart rate is higher than normal.</string>
    <string name="edit_your_age">Edit Your Age</string>
    <string name="save_update">Save &amp; Update></string>
    <string name="male">Male</string>
    <string name="female">Female</string>
    <string name="others">Others</string>
    <string name="type">Type</string>
    <string name="heart_rate_notes">Heart Rate Notes</string>
    <string name="heart_rate_history">Heart Rate History</string>
    <string name="edit_your_gender">Edit Your Gender</string>
    <string name="heart_rate_trends">Heart Rate Trends</string>
    <string name="heart_rate_statistics">Heart Rate Statistics</string>
    <string name="average">Average</string>
    <string name="min">Min.</string>
    <string name="max">Max</string>
    <string name="total">Total</string>
    <string name="instruction">Instruction</string>
    <string name="stay_still_until_the_measuring_is_done">Stay still until the measuring is done!</string>
    <string name="set_alarm">Set Alarm</string>
    <string name="cancel">Cancel</string>
    <string name="save">Save</string>
    <string name="set_alarm_for_heart_rate">Set Alarm For Heart Rate</string>
    <string name="repeat">Repeat</string>
    <string name="error_measure_please_try_again">Error measure. Please Try again</string>
    <string name="try_again">Try Again</string>
    <string name="delete">Delete</string>
    <string name="new_record">New Record</string>
    <string name="result">Result</string>
    <string name="your_heart_rate">Your Heart Rate</string>
    <string name="heart_rate_detail">Heart Rate Detail</string>
    <string name="view_all">View All</string>
    <string name="schedule_smart_alarms_for_health">Schedule smart alarms for health</string>
    <string name="back">Back</string>
    <string name="our_programmer_is_working_hard_on_development">Our programmer is working hard on development.</string>
    <string name="coming_soon">Coming Soon</string>
    <string name="ai_chatgpt">AI ChatGPT</string>
    <string name="sound">Sound</string>
    <string name="vibrate">Vibrate</string>
    <string name="alarm">Alarm</string>
    <string name="remind_me_to_record">Remind Me To Record</string>
    <string name="delete_confirm">Delete Confirm</string>
    <string name="are_you_sure_delete_this">Are you sure delete this</string>
    <string name="about_me">About Me</string>
    <string name="age">Age</string>
    <string name="gender">Gender</string>
    <string name="record">Record</string>
    <string name="history_statistics">History &amp; Statistics</string>
    <string name="bottom_navigation_tracker_title">Tracker</string>
    <string name="have_you_set_the_alarm_yet">Have You Set The Alarm Yet?</string>
    <string name="not_this_time">Not This Time</string>
    <string name="heart_rate_measure_notice">This measurement information is for reference only and cannot replace the measurement of specialized meters</string>
</strings>
"""

root = ET.fromstring(xml_data)

for string_element in root.findall('string'):
    name = string_element.text
    print(name)
