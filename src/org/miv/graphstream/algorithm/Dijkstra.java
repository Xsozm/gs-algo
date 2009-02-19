/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.Path;

/**
 * <p>
 * Dijkstra's algorithm is a greedy algorithm that solves the single-source
 * shortest path problem for a directed graph with non negative edge weights (<a
 * href="http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>).
 * </p>
 * <p>
 * This length can be the absolute length of the path ( a path with 3 edges has
 * a length of 3), it can also be computed considering other constraints
 * situated on the edges or on the nodes.
 * </p>
 * <p>
 * Note that Dijkstra's algorithm only computes with non-negative values.
 * </p>
 * <p>
 * 
 * @complexity O(n^2 + m) with n the number of nodes and m the number of edges.
 * 
 * @author Antoine Dutot
 * @author Yoann Pign�
 */
public class Dijkstra
{
	Node target = null;

	Node source = null;

	/**
	 * Distances depending on the observed attribute.
	 */
	Hashtable<Node, Double> distances;

	/**
	 * Lengths in number of links.
	 */
	Hashtable<Node, Double> length;

	/**
	 * Computes the Dijkstra's algorithm on the given graph starting from the
	 * given source node, considering the given attribute locates on the given
	 * kind of elements (nodes or edges).
	 * @param graph The Graph on witch the algorithm will construct a shortest
	 *        path tree.
	 * @param element The kind of element observed in the graph.
	 * @param attribute The attribute considered for the distance computation.
	 * @param start The root node of the shortest path tree.
	 */
	@SuppressWarnings("all")
	public Dijkstra( Graph graph, Element element, String attribute, Node start )
	{
		distances = new Hashtable<Node, Double>();
		length = new Hashtable<Node, Double>();
		ArrayList<Node> computed = new ArrayList<Node>();
		Collection<Edge> edges;
		Edge runningEdge;
		double dist;
		double len;
		Node runningNode;
		Node neighborNode;
		PriorityList<Node> priorityList = new PriorityList<Node>();
		priorityList.insertion( start, 0.0 );
		distances.put( start, 0.0 );
		length.put( start, 0.0 );
		source = start;

		// initialization
		for( Node v: ( (Graph) graph ).getNodeSet() )
		{
			v.removeAttribute( "Dijkstra.parentEdges" );
		}

		while( !priorityList.isEmpty() )
		{
			runningNode = priorityList.lire( 0 );
			edges = runningNode.getLeavingEdgeSet();
			Iterator<Edge> aretesIterator = (Iterator<Edge>) edges.iterator();

			while( aretesIterator.hasNext() )
			{
				runningEdge = ( (Edge) aretesIterator.next() );
				neighborNode = runningEdge.getOpposite( runningNode );

				if( !computed.contains( neighborNode ) )
				{
					double val = 0;
					if( attribute == null )
					{
						val = 1.0;
					}
					else
					{
						if( element == Element.edge )
						{
							val = ( (Double) runningEdge.getAttribute( attribute ) );
						}
						else
						{
							val = ( (Double) neighborNode.getAttribute( attribute ) );
						}
					}
					if( val < 0 )
					{
						throw new NumberFormatException( "Attribute \"" + attribute + "\" has a negative value on element "
								+ ( element == Element.edge ? runningEdge.toString() : neighborNode.toString() ) );
					}
					dist = (int) ( distances.get( runningNode ) + val );
					len = (int) ( length.get( runningNode ) + 1 );

					if( priorityList.containsKey( neighborNode ) )
					{
						if( dist <= distances.get( neighborNode ) )
						{
							if( dist == distances.get( neighborNode ) )
							{
								( (ArrayList<Edge>) neighborNode.getAttribute( "Dijkstra.parentEdges" ) ).add( runningEdge );
							}
							else
							{
								ArrayList<Edge> parentEdges = new ArrayList<Edge>();
								parentEdges.add( runningEdge );
								neighborNode.addAttribute( "Dijkstra.parentEdges", parentEdges );

								distances.put( neighborNode, dist );
								neighborNode.addAttribute( "label", neighborNode.getId() + " - " + dist );
								length.put( neighborNode, len );

								priorityList.suppression( neighborNode );
								priorityList.insertion( neighborNode, dist );
							}
						}
					}
					else
					{
						priorityList.insertion( neighborNode, dist );
						distances.put( neighborNode, dist );
						length.put( neighborNode, len );
						ArrayList<Edge> parentEdges = new ArrayList<Edge>();
						parentEdges.add( runningEdge );
						neighborNode.addAttribute( "Dijkstra.parentEdges", parentEdges );

					}

				}
			}
			priorityList.suppression( runningNode );
			computed.add( runningNode );
		}

	}

	@SuppressWarnings("unchecked")
	private void facilitate_getShortestPaths( List<Edge> g, Node v )
	{
		if( v == source )
		{
			return;
		}
		ArrayList<Edge> list = (ArrayList<Edge>) v.getAttribute( "Dijkstra.parentEdges" );
		if( list == null )
		{
			System.out.println( "The list of parent Edges  is null, v=" + v.toString() + " source=" + source.toString() );
		}
		else
		{
			for( Edge l: list )
			{
				g.add( l );
				facilitate_getShortestPaths( g, l.getOpposite( v ) );
			}
		}
	}

	/**
	 * Returns the shortest path between the source node and one given target
	 * one. If multiple shortest paths exist, a of them is returned at random.
	 * @param target the target of the shortest path starting at the source node
	 *        given in the constructor.
	 * @return A {@link org.miv.graphstream.graph.Path} object that constrains
	 *         the list of nodes and edges that constitute it.
	 */
	public Path getShortestPath( Node target )
	{
		if( target == source )
		{
			return null;
		}
		Path p = new Path();
		boolean noPath = false;
		Node v = target;
		while( v != source && !noPath )
		{
			ArrayList<? extends Edge> list = (ArrayList<? extends Edge>) v.getAttribute( "Dijkstra.parentEdges" );
			if( list == null )
			{
				noPath = true;
			}
			else
			{
				Edge parentEdge = list.get( 0 );

				// --- DEBUG ---
				// if( parentEdge == null )
				// {
				// System.out.println( "parentEdge is null, v=" + v.toString() +
				// " source=" + source.toString() );
				// }

				p.add( v, parentEdge );
				v = parentEdge.getOpposite( v );
			}
		}
		return p;
	}

	/**
	 * Constructs all the possible shortest paths from the source node to the
	 * destination (end). Warning: this construction is VERY HEAVY !
	 * 
	 * @param end The destination to which shortest paths are computed.
	 * @return a list of shortest paths given with
	 *         {@link org.miv.graphstream.graph.Path} objects.
	 */
	public List<Path> getPathSetShortestPaths( Node end )
	{
		System.out.println( "getPathSetShortestPaths" );
		ArrayList<Path> paths = new ArrayList<Path>();
		pathSetShortestPath_facilitate( end, new Path(), paths );
		return paths;
	}

	private void pathSetShortestPath_facilitate( Node current, Path path, List<Path> paths )
	{
		if( current != source )
		{
			Node next = null;
			ArrayList<? extends Edge> parentEdges = (ArrayList<? extends Edge>) current.getAttribute( "Dijkstra.parentEdges" );
			while( current != source && parentEdges.size() == 1 )
			{
				Edge e = parentEdges.get( 0 );
				next = e.getOpposite( current );
				path.add( current, e );
				current = next;
				parentEdges = (ArrayList<? extends Edge>) current.getAttribute( "Dijkstra.parentEdges" );
			}
			if( current != source )
			{
				for( Edge e: parentEdges )
				{
					Path p = path.getACopy();
					p.add( current, e );
					pathSetShortestPath_facilitate( e.getOpposite( current ), p, paths );

				}
			}
		}
		if( current == source )
		{
			paths.add( path );
		}
	}

	/**
	 * Synonym to {@link #getEdgeSetShortestPaths(Node)}.
	 * @see #getEdgeSetShortestPaths(Node)
	 * @param target The target node for the shortest path.
	 * @return A list of edges.
	 */
	@Deprecated
	public List<Edge> getShortestPaths( Node target )
	{
		return getEdgeSetShortestPaths( target );
	}

	/**
	 * Returns a set of edges that compose the shortest path. If more than one
	 * path is the shortest one, the edges are included in the returned set of
	 * edges.
	 * @param target The endpoint of the path to compute from the source node
	 *        given in the constructor.
	 * @return The set of edges that belong the the solution.
	 */
	public List<Edge> getEdgeSetShortestPaths( Node target )
	{
		if( target == source )
		{
			System.out.println( "end=source !!!" );
			return null;
		}
		List<Edge> g = new ArrayList<Edge>();
		Node v = target;
		facilitate_getShortestPaths( g, v );
		return g;
	}

	/**
	 * Returns the value of the shortest path between the source node and the
	 * given target according to the attribute specified in the constructor.
	 * @param target The endpoint of the path to compute from the source node
	 *        given in the constructor.
	 * @return A numerical value that represent the distance of the shortest
	 *         path.
	 */
	public double getShortestPathValue( Node target )
	{
		return distances.get( target );
	}

	/**
	 * Returns the number of edges in the shortest path from the source to the
	 * given target.
	 * @param target The node to compute the shortest path to.
	 * @return the number of edges in the shortest path.
	 */
	public double getShortestPathLength( Node target )
	{
		return length.get( target );
	}

	/**
	 * This enumeration help identifying the kind of element to be used to
	 * compute the shortest path.
	 */
	public static enum Element
	{
		edge, node
	}

}

class PriorityList<E>
{

	ArrayList<E> objets;

	ArrayList<Double> priorites;

	int taille;

	public PriorityList()
	{
		objets = new ArrayList<E>();
		priorites = new ArrayList<Double>();
		taille = 0;
	}

	public boolean containsKey( E objet )
	{
		boolean contient = false;
		if( objets.contains( objet ) )
		{
			contient = true;
		}
		return contient;
	}

	public void insertion( E element, double prio )
	{
		boolean trouve = false;
		int max = priorites.size();
		int i = 0;
		while( ( !trouve ) && ( i < max ) )
		{
			if( priorites.get( i ) > prio )
			{
				trouve = true;
			}
			else
			{
				i++;
			}
		}
		if( i == max )
		{
			objets.add( element );
			priorites.add( prio );
		}
		else
		{
			objets.add( i, element );

			// MODIF ICI !
			// priorites.add(prio);
			priorites.add( i, prio );
		}
	}

	public boolean isEmpty()
	{
		boolean vide = false;
		if( objets.size() == 0 )
		{
			vide = true;
		}
		return vide;
	}

	public E lire( int position )
	{
		return objets.get( position );
	}

	public int size()
	{
		return objets.size();
	}

	public void suppression( E element )
	{
		int position = objets.lastIndexOf( element );
		objets.remove( position );
		priorites.remove( position );
	}

	public String toString()
	{
		String laliste = new String( " -------- Liste --------- \n" );
		for( int i = 0; i < objets.size(); i++ )
		{
			laliste = laliste + objets.get( i ).toString() + ":::" + priorites.get( i ).toString() + "\n";
		}
		return laliste;
	}
}