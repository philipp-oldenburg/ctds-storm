/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test;

import org.ddogleg.clustering.AssignCluster;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.FactoryClustering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Demonstration of how to cluster a N-dimensional points into clusters.  The high level interface provides
 * the capability to do hard and soft assignments.  Hard assignments select a single best match for a point
 * and soft assignments compute a weight for each cluster, high weights mean better matches.
 *
 * Results are visualized in a window.  Points in different clusters are assigned different colors.  If you
 * click the inside of the window the algorithm is run again and a new set of clusters is shown.
 *
 * @author Peter Abeles
 */
public class Test {

	// if true it will create the clusters from a gaussian distribution.
	// Otherwise an uniform distribution is used.
	public static boolean gaussian = true;
	public static Random rand = new Random();
	public static boolean clicked = false;

	public static List<double[]> createCluster( double x , double y , double width , int N ) {

		List<double[]> points = new ArrayList<double[]>();

		for (int i = 0; i < N; i++) {
			double[] p = new double[2];

			if ( gaussian ) {
				p[0] = rand.nextGaussian()*width/3+x;
				p[1] = rand.nextGaussian()*width/3+y;
			} else {
				p[0] = rand.nextDouble()*width-width/2+x;
				p[1] = rand.nextDouble()*width-width/2+y;
			}

			points.add(p);
		}

		return points;
	}

	public static void main(String[] args) {
		List<double[]> points = new ArrayList<double[]>();

		// create 3 clusters drawn from a uniform square distribution
		points.addAll( createCluster(4,4,2,100) );
		points.addAll( createCluster(8,4,1,120) );
		points.addAll( createCluster(6,5,4,300) );

		// remove any structure from the point's ordering
		Collections.shuffle(points);

		ComputeClusters<double[]> cluster = FactoryClustering.kMeans_F64(null,1000,100, 1e-8);
//		ComputeClusters<double[]> cluster = FactoryClustering.gaussianMixtureModelEM_F64(1000, 1e-8);

		cluster.init(2, rand.nextLong());

		// visualization stuff
		Gui gui = new Gui(points);

		JFrame frame = new JFrame();
		frame.add(gui,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Run the cluster algorithm again each time the user clicks the window
		// This allows you to see how stable the clusters are
		while( true ) {

			cluster.process(points, 3);

			AssignCluster<double[]> assignment = cluster.getAssignment();
			gui.update(assignment);

			while( !clicked ) {
				Thread.yield();
			}
			clicked = false;
		}
	}

	/**
	 * Basic visualization which draws the points in a window.  Each color is assigned a different random color
	 */
	public static class Gui extends JPanel implements MouseListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		AssignCluster<double[]> assignment;
		List<double[]> points;
		Color colors[];

		public Gui( List<double[]> points) {

			this.points = points;

			setPreferredSize(new Dimension(300,300));
			setBackground(Color.WHITE);

			addMouseListener(this);
		}

		public synchronized void update( AssignCluster<double[]> assignment ) {
			this.assignment = assignment;
			colors = new Color[ assignment.getNumberOfClusters() ];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = new Color(rand.nextInt() | 0x080808);
			}
			repaint();
		}

		@Override
		public synchronized void paintComponent(Graphics g) {
			if( assignment == null )
				return;

			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D)g;

			double scaleX = getWidth()/10.0;
			double scaleY = getHeight()/10.0;


			for (int i = 0; i < points.size(); i++) {
				double[] p = points.get(i);
				int x = (int)(p[0]*scaleX+0.5);
				int y = (int)(p[1]*scaleY+0.5);

				g2.setColor(colors[assignment.assign(p)]);
				g2.fillOval(x-2,y-2,5,5);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			clicked = true;
		}

		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
	}
}
