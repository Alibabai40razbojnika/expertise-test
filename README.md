# expertise-test
Expertise test project

First of all, hello

Presented solution is intended for storing, manipulating and presenting data

Problem:

Files are stored on a local machine (via some service) and need to be uploaded daily to Google Cloud Storage (in further text GCS)

After being uploaded, files need to be formated and stored into Google Cloud BigQuery (in further text GCBQ) for further use

Data in GCBQ need to be refreshed every hour depending on files in GCS

Data needs to be represented on a web page

Solution:

Bucket for storing data is created in GCS

Application has a scheduled job that pulls files from local repository and push files into GCS bucket every day 

Google Cloud Functions (in further text GCF) is used to store app (as a single class with depending properties) that contains function that will react on GCS file push in order to transform and inject data into GCBQ 

GCBQ contains data set in which table is created for storing transformed data from GCS via GCF

After data is stored in GCBQ, rest service is implemented for getting the data for specific tractor (serial number parameter) on a specific day (date parameter)

On a web page, form is created with two fields, serial number and date, for which website will provide information and tractor route for that day on Google Map presented on a page

Dependencies:

User of this product needs to have google account.
With account user can enable services (GCS, GCF, GCBQ and their dependencies)
User needs to create project in which will store, manipulate and view data.

After, user need to have prepared names for GCS bucket, GCBQ dataset and table (they need to be unique).
Rest calls have been prepared in order not to create bucket, dataset and table manually.

Google Map api key needs to be prepared in order to look at tractor path.

For GCF, project needs manualy to be pushed on GCF in order to automatically pull, transform and insert formated data into GCBQ table for further use.

Also, credentials json file needs to be downloaded from GCP and be available for application purposes.

GCF:

For a GCF zip file is prepared inside main application and needs manualy to be pushed into GCF.
Before doing that, two json files need to be filled correctly.
properties.json - values that have been prepared 
credentials.json - file containing credentials previously downloaded

After that, creation of the function on GCF with same location used in GCS and GCBQ
function name is creators choice
region needs to be same region used in GCS and GCBQ (ex. europe-central2)
trigger type is Cloud Storage
event type is Finalize/Create
bucket is previously created bucket

Save and go next

select Java 11 as runtime
source code choose ZIP Upload
upload previously prepared zip file with function (zip file containing only src and pom files)
stage bucket is previously created bucket
entry point is the default (com.example.Example)
if deploy button is unavailable, just click on entry point field

Deploy your function (it needs about 2 minutes to be deployed)

Main application:

In order for application to work correctly custom.properties file needs fo be filled correctly

Application workflow:

After all dependencies have been met user can fill necessary property files and upload function to GCF

User can run the app (ex. mvn exec:java -Dexec.mainClass=com.expertise.filip.FilipApplication) 
and run next commands 
to create big query dataset
curl -X POST http://localhost:8080/setup/bigquery/dataset
to create big query table
curl -X POST http://localhost:8080/setup/bigquery/formatedtable
to create storage bucket
curl -X POST http://localhost:8080/setup/storage/bucket

(in this example local machine is where code is run)

After that everything is set in order for application to work correctly

In the local folder there user can put files which need to be saved in GCS
Note: initial delay has been put for 30 minutes in order for everything to be set before jobs start

First job takes all the files (filtered by regex property) and imports them to GCS
After file upload, in local folder that file gets moved to backup folder

After file upload to GCS has finished, function in GCF is called and it transforms data from that file into GCBQ table

Second job, takes files in GCS and with values from those files it updates table in GCBQ

First job occures once in 24h, second one occures every hour
Note: those times can be changed in application.properties file, values are in seconds

Now files are imported in GCS and GCBQ and with rest calls can be used

Rest URI for getting information for a single tractor at single day 
/query/select/serialNumber/{serialNumber}/date/{date}

Serial Number parameter is regex formated with first letter 'A' and next come digits
Date parameter is regex formated with format dd.mm.yyyy

Website contains index page, tractor form and tractor data page.
From index page you can go to form page and there is form with two input fields, one for serial number, other for date.

After submitting correct data user will get to information page with information from formated table in GCBQ and Google Map showing tractors path for that day.


