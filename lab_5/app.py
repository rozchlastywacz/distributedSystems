from os import abort
from flask import Flask, render_template, request
import requests

API_KEY = "4526d487f12ef78b82b7a7d113faea64"
WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&"
POLLUTION_URL = "http://api.openweathermap.org/data/2.5/air_pollution/forecast?"
app = Flask(__name__)

@app.route('/', methods=['GET'])
def main():
    return render_template('index.html')

@app.route('/weather', methods=['POST'])
def weather_form():
    print(request.form)
    print(request.headers)
    city = request.form['city']
    if request.form['days'].isnumeric():
        days = int(request.form['days'])
        if days > 0 and days < 18:
            weather_request_string = f'{WEATHER_URL}q={city}&cnt={days}&appid={API_KEY}'
            print(weather_request_string)
            weather_request = requests.get(weather_request_string)
            wr_json = weather_request.json()
            if wr_json["cod"] == "200":
                weather_list = wr_json.get('list')
                avg_day = avg_night =  avg_pressure = avg_humidity = avg_clouds = 0
                for entry in weather_list:
                    avg_day += entry['temp']['morn']
                    avg_night += entry['temp']['eve']
                    avg_pressure += entry['pressure']
                    avg_humidity += entry['humidity']
                    avg_clouds += entry['clouds']
                avg_day = avg_day/days
                avg_night = avg_night/days
                avg_pressure = avg_pressure/days
                avg_humidity = avg_humidity/days
                avg_clouds = avg_clouds/days
                lat = wr_json['city']['coord']['lat']
                lon = wr_json['city']['coord']['lon']
                pollution_request_string = f'{POLLUTION_URL}lat={lat}&lon={lon}&appid={API_KEY}'
                print(pollution_request_string)
                pollution_request = requests.get(pollution_request_string)
                if pollution_request.status_code == 200:
                    pr_json = pollution_request.json()
                    pollution_list = pr_json['list']
                    air_quality_index = pm_2_5 = pm_10 = 0
                    for entry in pollution_list[:days*24]:
                        air_quality_index += entry['main']['aqi']
                        pm_2_5 += entry['components']['pm2_5']
                        pm_10 += entry['components']['pm10']
                    air_quality_index = air_quality_index/(days*24)
                    pm_2_5 = pm_2_5/(days*24)
                    pm_10 = pm_10/(days*24)
                    return render_template(
                        'weather_pollution.html', 
                        city=city, 
                        days=days, 
                        avg_day=avg_day, 
                        avg_night=avg_night, 
                        avg_pressure=avg_pressure, 
                        avg_humidity=avg_humidity, 
                        avg_clouds=avg_clouds, 
                        air_quality_index=air_quality_index, 
                        pm_2_5=pm_2_5, pm_10=pm_10)    
                return render_template(
                    'weather.html', 
                    city=city, 
                    days=days, 
                    avg_day=avg_day, 
                    avg_night=avg_night, 
                    avg_pressure=avg_pressure, 
                    avg_humidity=avg_humidity, 
                    avg_clouds=avg_clouds)    
            elif wr_json["cod"] == "404":
                wr_json_msg = wr_json['message']
                return f'Wystąpił błąd: {wr_json_msg}'
        else:
            return "Liczba dni powinna być między 1 a 17"
    else:
        return "Liczba dni powinna być liczbą"

@app.route('/weather/<city>/<days>', methods=['GET'])
def weather(city, days):
    r = requests.get(f'{WEATHER_URL}q={city}&cnt={days}&appid={API_KEY}')
    print(r.json)
    return r.content

if __name__ == '__main__':
    app.run(debug=True)

