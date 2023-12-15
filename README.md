```
% cd src/test/resources/rhone
% wms-tile-get -s grand-lyon-wms.json \
    -g departement-69-rhone-1-sur-400-sur-20-polygones.geojson \
    -z 20 \
    -o grand-lyon-1
Total: 850, Ok: 850, Failed: 0, Skipped: 0 <-- TODO: check that failed and skipped
Finished in 6 mn <-- TODO: too borderline as still need to upload to S3 ==> use 1/400/40 instead of 1/400/20
TODO: variabilize frontal and worker timeout in POJA
```

```
% wms-tile-get -s grand-lyon-wms.json \
               -g departement-69-rhone-grand-lyon-1-sur-1000-polygones.geojson \ 
               -z 20 \
               -o grand-lyon-2
Total: 883, Ok: 883, Failed: 0, Skipped: 0
Finished in 6 minutes
```