package io.sclera.utils.ips;

import org.springframework.stereotype.Component;

@Component
public class IPSUtils {
    public Coordinate centroid(double x1, double y1, double x2, double y2, double x3, double y3) {

        Coordinate coordinate = new Coordinate();
        coordinate.setX((x1+x2+x3)/3);
        coordinate.setY((y1+y2+y3)/3);
        return coordinate;
    }

    public double inCircle(double x0, double y0, double r0, double x, double y) {
        double res  = Math.pow((x - x0), 2) + Math.pow((y - y0), 2) - r0*r0;
        return res;
    }


    public Coordinate nearestPoint(double x0, double y0, double r0, double x1, double y1, double x2, double y2) {

        double dx1, dx2, dy1, dy2, d1, d2, rd1, rd2;
        Coordinate coordinate = new Coordinate();
        dx1 = x1 - x0;
        dy1 = y1 - y0;
        dx2 = x2 - x0;
        dy2 = y2 - y0;


        /* Determine the straight-line distance between the centers. */
        d1 = Math.sqrt((dy1*dy1) + (dx1*dx1));
        d2 = Math.sqrt((dy2*dy2) + (dx2*dx2));


        rd1 = Math.abs(d1 - r0);
        rd2 = Math.abs(d2 - r0);

        System.out.println(d1);
        System.out.println(d2);
        System.out.println(rd1);
        System.out.println(rd1);

        if(rd1<rd2) {
            coordinate.setX(x1);
            coordinate.setY(y1);
        }
        else {
            coordinate.setX(x2);
            coordinate.setY(y2);
        }
        return coordinate;
    }

    public Coordinate triangulation(double x1, double y1, double r1, double x2, double y2, double r2, double x3, double y3, double r3) {
        Coordinate coordinate = new Coordinate();
        Coordinate out1[], out2[], out3[], finalOut1 = null, finalOut2= null, finalOut3 = null;
        out1 = calculateTwoCircleIntersection(x1, y1, r1, x2, y2, r2);
        if(out1[0].getX() != Double.MAX_VALUE && out1[0].getX() != Double.MIN_VALUE) {

            if(inCircle(x3, y3, r3, out1[0].getX(), out1[0].getY()) < 0) {
                finalOut1 = out1[0];
            }
            if(inCircle(x3, y3, r3, out1[1].getX(), out1[1].getY()) < 0) {
                if(finalOut1 != null) {
                    finalOut1 = nearestPoint(x3, y3, r3, out1[0].getX(), out1[0].getY(), out1[1].getX(), out1[1].getY());
                }
                else {
                    finalOut1 = out1[1];
                }

            }
            if(finalOut1 == null) {
                finalOut1 = nearestPoint(x3, y3, r3, out1[0].getX(), out1[0].getY(), out1[1].getX(), out1[1].getY());
            }
        }

        out2 = calculateTwoCircleIntersection(x1, y1, r1, x3, y3, r3);
        if(out2[0].getX() != Double.MAX_VALUE && out2[0].getX() != Double.MIN_VALUE) {

            if(inCircle(x2, y2, r2, out2[0].getX(), out2[0].getY()) < 0) {
                finalOut2 = out2[0];
            }
            if(inCircle(x2, y2, r2, out2[1].getX(), out2[1].getY()) < 0) {
                if(finalOut2 != null) {
                    finalOut2 = nearestPoint(x2, y2, r2, out2[0].getX(), out2[0].getY(), out2[1].getX(), out2[1].getY());
                }
                else {
                    finalOut2 = out2[1];
                }

            }
            if(finalOut2 == null) {
                finalOut2 = nearestPoint(x2, y2, r2, out2[0].getX(), out2[0].getY(), out2[1].getX(), out2[1].getY());
            }
        }

        out3 = calculateTwoCircleIntersection(x2, y2, r2, x3, y3, r3);
        if(out3[0].getX() != Double.MAX_VALUE && out3[0].getX() != Double.MIN_VALUE) {

            if(inCircle(x1, y1, r1, out3[0].getX(), out3[0].getY()) < 0) {
                finalOut3 = out3[0];
            }
            if(inCircle(x1, y1, r1, out3[1].getX(), out3[1].getY()) < 0) {
                if(finalOut3 != null) {
                    finalOut3 = nearestPoint(x1, y1, r1, out3[0].getX(), out3[0].getY(), out3[1].getX(), out3[1].getY());
                }
                else {
                    finalOut3 = out3[1];
                }
            }
            if(finalOut3 == null) {
                finalOut3 = nearestPoint(x1, y1, r1, out3[0].getX(), out3[0].getY(), out3[1].getX(), out3[1].getY());
            }
        }

        System.out.println(finalOut1.toString() + finalOut2.toString() + finalOut3.toString());
        coordinate = centroid(finalOut1.getX(), finalOut1.getY(),finalOut2.getX(), finalOut2.getY(),finalOut3.getX(), finalOut3.getY());
        return coordinate;
    }

    public Coordinate[] calculateTwoCircleIntersection(double x0, double y0, double r0,
                                                       double x1, double y1, double r1)
    {
        double a, dx, dy, d, h, rx, ry;
        double point2_x, point2_y;
        Coordinate coordinate[] = new Coordinate[2];
        coordinate[0] = new Coordinate();
        coordinate[1] = new Coordinate();
        /* dx and dy are the vertical and horizontal distances between
         * the circle centers.
         */
        dx = x1 - x0;
        dy = y1 - y0;

        /* Determine the straight-line distance between the centers. */
        d = Math.sqrt((dy*dy) + (dx*dx));

        /* Check for solvability. */
        if (d > (r0 + r1))
        {
            /* no solution. circles do not intersect. */

//			double ratio_cut1=(r0+(d-(r0+r1))/2)/d;
//			double ratio_cut2=(r1+(d-(r0+r1))/2)/d;

            double ratio_cut1= r0;
            double ratio_cut2= r1;

            double intersectionPoint1_x =(ratio_cut1*x1+ratio_cut2*x0)/(ratio_cut1+ratio_cut2);
            double intersectionPoint2_x = (ratio_cut1*x1+ratio_cut2*x0)/(ratio_cut1+ratio_cut2);
            double intersectionPoint1_y =(ratio_cut1*y1+ratio_cut2*y0)/(ratio_cut1+ratio_cut2);
            double intersectionPoint2_y = (ratio_cut1*y1+ratio_cut2*y0)/(ratio_cut1+ratio_cut2);

            coordinate[0].setX(intersectionPoint1_x);
            coordinate[0].setY(intersectionPoint1_y);
            coordinate[1].setX(intersectionPoint2_x);
            coordinate[1].setY(intersectionPoint2_y);

            System.out.println("NO solution" + coordinate[0].toString());
            //coordinate[0].setX(Double.MIN_VALUE);
            return coordinate;
        }
        if (d < Math.abs(r0 - r1))
        {
            /* no solution. one circle is contained in the other */

            Coordinate tempcoordinate[], tempcoor;
            if(r0 < r1) {
                tempcoordinate = getCircleLineIntersectionPoint(x0, y0, r0, x0, y0, x1, y1);
                tempcoor = nearestPoint(x1, y1, r1, tempcoordinate[0].getX(), tempcoordinate[0].getY(), tempcoordinate[1].getX(), tempcoordinate[1].getY());
                coordinate[0].setX(tempcoor.getX());
                coordinate[0].setY(tempcoor.getY());
                coordinate[1].setX(tempcoor.getX());
                coordinate[1].setY(tempcoor.getY());
            }else {
                tempcoordinate = getCircleLineIntersectionPoint(x1, y1, r1, x0, y0, x1, y1);
                tempcoor = nearestPoint(x0, y0, r0, tempcoordinate[0].getX(), tempcoordinate[0].getY(), tempcoordinate[1].getX(), tempcoordinate[1].getY());
                coordinate[0].setX(tempcoor.getX());
                coordinate[0].setY(tempcoor.getY());
                coordinate[1].setX(tempcoor.getX());
                coordinate[1].setY(tempcoor.getY());
            }

            //coordinate[0].setX(Double.MAX_VALUE);
            System.out.println("Inside circle" + coordinate[0].toString());
            //System.out.println(coordinate[0].toString());
            return coordinate;
        }

        /* 'point 2' is the point where the line through the circle
         * intersection points crosses the line between the circle
         * centers.
         */

        /* Determine the distance from point 0 to point 2. */
        a = ((r0*r0) - (r1*r1) + (d*d)) / (2.0 * d) ;

        /* Determine the coordinates of point 2. */
        point2_x = x0 + (dx * a/d);
        point2_y = y0 + (dy * a/d);

        /* Determine the distance from point 2 to either of the
         * intersection points.
         */
        h = Math.sqrt((r0*r0) - (a*a));

        /* Now determine the offsets of the intersection points from
         * point 2.
         */
        rx = -dy * (h/d);
        ry = dx * (h/d);

        /* Determine the absolute intersection points. */
        double intersectionPoint1_x = point2_x + rx;
        double intersectionPoint2_x = point2_x - rx;
        double intersectionPoint1_y = point2_y + ry;
        double intersectionPoint2_y = point2_y - ry;

        System.out.println("INTERSECTION Circle1 AND Circle2:"+ "(" + intersectionPoint1_x + "," + intersectionPoint1_y + ")" + " AND (" + intersectionPoint2_x + "," + intersectionPoint2_y + ")");

        coordinate[0].setX(intersectionPoint1_x);
        coordinate[0].setY(intersectionPoint1_y);
        coordinate[1].setX(intersectionPoint2_x);
        coordinate[1].setY(intersectionPoint2_y);
        return coordinate;
    }

    public Coordinate [] getCircleLineIntersectionPoint(double x0, double y0, double r0, double x1, double y1, double x2, double y2) {

        double baX = x2 - x1;
        double baY = y2 - y1;
        double caX = x0 - x1;
        double caY = y0 - y1;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - r0 * r0;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return null;
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;
        Coordinate coordinate [] = new Coordinate[2];
        coordinate[0] = new Coordinate();
        coordinate[1] = new Coordinate();

        coordinate[0].setX(x1 - baX * abScalingFactor1);
        coordinate[0].setY(y1 - baY * abScalingFactor1);

        coordinate[1].setX(x1 - baX * abScalingFactor2);
        coordinate[1].setY(y1 - baY * abScalingFactor2);
        System.out.println("line intersection coordinate" + coordinate[0].toString() + coordinate[1].toString()) ;


        return coordinate;
    }



    public double calculateWifiDeviceDistanceBySignalLevelAndFrequency(double signalLevelInDb, double freqInMHz) {
        /*
        -27.55 - fixed rssi dbm value for 1 meter
        resulting value is in meter
         */
//        double exp = (Math.abs(signalLevelInDb) - (-27.55) - (20 * Math.log10(freqInMHz/1000)))/ 20.0;
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public double calculateBluetoothDeviceDistanceBySignalLevel(Double signalLevelInDb) {
        /*
        -69 - fixed rssi dbm value for 1 meter
        resulting value is in meter
         */
        double exp = (((-69) - signalLevelInDb) / 20.0);
        return Math.pow(10.0, exp);
    }
}

