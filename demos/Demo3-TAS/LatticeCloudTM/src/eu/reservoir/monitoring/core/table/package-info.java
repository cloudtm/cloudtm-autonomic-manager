/**
 * This package provides classes that 
 * are used for embedding Table data within a Measurement.
 * <p>
 * A Table is defined using a TableHeader, like so:
 * <pre>
 *      Table table1 = new DefaultTable();
 *
 * 	TableHeader header = new DefaultTableHeader().
 *          add("name", ProbeAttributeType.STRING).
 *          add("type", ProbeAttributeType.STRING);
 *
 *      table1.defineTable(header);
 * </pre>
 * which defines a name and a type for each column.
 * Rows are added to the table using TableRow objects
 * like so:
 * <pre>
 * 	TableRow r0 = new DefaultTableRow().
 *          add(new DefaultTableValue("stuart")).
 *          add(new DefaultTableValue("person"));
 *
 *      table1.addRow(r0);
 * </pre>
 */
package eu.reservoir.monitoring.core.table;
