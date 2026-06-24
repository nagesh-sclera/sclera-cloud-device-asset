#!/bin/bash
echo "Script Started..."
while :; do
    case $1 in
    -file)
        if [ "$1" ] && [ "$2" ]; then
            fileName=$2
            shift
        else
            echo 'ERROR: "--file" requires a non-empty option argument.'
            exit 0;
        fi
        ;;
    -ulx)
        if [ "$1" ] && [ "$2" ]; then
            ulx=$2
            shift
        else
            echo 'ERROR: "--ulx" requires a non-empty option argument.'
            exit 0;
        fi
        ;;  
    -uly)
        if [ "$1" ] && [ "$2" ]; then
            uly=$2
            shift
        else
            echo 'ERROR: "--uly" requires a non-empty option argument.'
            exit 0;
        fi
        ;; 
    -llx)
        if [ "$1" ] && [ "$2" ]; then
            llx=$2
            shift
        else
            echo 'ERROR: "--llx" requires a non-empty option argument.'
            exit 0;
        fi
        ;; 
    -lly)
        if [ "$1" ] && [ "$2" ]; then
            lly=$2
            shift
        else
            echo 'ERROR: "--lly" requires a non-empty option argument.'
            exit 0;
        fi
        ;;                              
    -?*)
        printf 'WARN: Unknown option\n'
        exit 0; 
        ;;
    *)
        break
        ;;
    esac
    shift
done
if [ ! -f "$fileName" ]; then
    echo "$fileName not Found."
    exit 0;    
fi
data=`gdalinfo -json $fileName`
key='colorTable'
dir=$(dirname "$fileName")
if [[ "$data" == *"$key"* ]]; then
    gdal_translate -of GTiff -a_srs EPSG:4326 -a_ullr $ulx $uly $llx $lly $fileName $dir/temp.tif
    gdal_translate -of GTiff -expand rgba $dir/temp.tif $dir/output.tif
    gdal2tiles.py --processes=2 -w none $dir/output.tif $dir
    rm $dir/temp.tif $dir/output.tif $fileName
else
    gdal_translate -of GTiff -a_srs EPSG:4326 -a_ullr $ulx $uly $llx $lly $fileName $dir/output.tif
    gdal2tiles.py --processes=2 -w none $dir/output.tif $dir
    rm $dir/output.tif $fileName
fi
echo "done"