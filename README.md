```
% cd src/test/resources/rhone
% wms-tile-get -s lyon_wms.json \
    -g departement-69-rhone-1-sur-400-sur-20-polygones.geojson \
    -z 20 \
    -o lyon
Total: 850, Ok: 850, Failed: 0, Skipped: 0 <-- TODO: check that failed and skipped
Finished in 6 mn <-- TODO: too borderline as still need to upload to S3 ==> use 1/400/40 instead of 1/400/20
TODO: variabilize frontal and worker timeout in POJA
```
