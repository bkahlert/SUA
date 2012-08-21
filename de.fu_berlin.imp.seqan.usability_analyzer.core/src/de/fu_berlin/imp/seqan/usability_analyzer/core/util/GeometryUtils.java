package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GeometryUtils {

	public static double[] convert(Point a) {
		return new double[] { a.x, a.y };
	}

	public static double shortestDistance(double[] a, double[] b, double[] p) {
		double[] p2 = new double[] { b[0] - a[0], b[1] - a[1] };
		double u = ((p[0] - a[0]) * p2[0] + (p[1] - a[1]) * p2[1])
				/ (p2[0] * p2[0] + p2[1] * p2[1]);

		if (u > 1)
			u = 1;
		else if (u < 0)
			u = 0;

		double x = a[0] + u * p2[0];
		double y = a[1] + u * p2[1];

		double dx = x - p[0];
		double dy = y - p[1];

		return Math.sqrt(dx * dx + dy * dy);
	}

	public static double shortestDistance(Point a, Point b, Point p) {
		return shortestDistance(convert(a), convert(b), convert(p));
	}

	public static double shortestDistance(double[] a, double[] b, double[] c,
			double[] d, double[] p) {
		double d_ab = shortestDistance(a, b, p);
		double d_bc = shortestDistance(b, c, p);
		double d_cd = shortestDistance(c, d, p);
		double d_da = shortestDistance(d, a, p);
		return Math.min(Math.min(d_ab, d_bc), Math.min(d_cd, d_da));
	}

	public static double shortestDistance(Rectangle rect, Point p) {
		double[] a = new double[] { rect.x, rect.y };
		double[] b = new double[] { rect.x + rect.width, rect.y };
		double[] c = new double[] { rect.x + rect.width, rect.y + rect.height };
		double[] d = new double[] { rect.x, rect.y + rect.height };
		return shortestDistance(a, b, c, d, convert(p));
	}

	public static boolean isWithin(Rectangle rect, Point p) {
		return p.x >= rect.x && p.x <= rect.x + rect.width && p.y >= rect.y
				&& p.y <= rect.y + rect.height;
	}
}
