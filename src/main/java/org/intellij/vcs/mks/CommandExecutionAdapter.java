/*
 * COPYRIGHT. HSBC HOLDINGS PLC 2008. ALL RIGHTS RESERVED.
 *
 * This software is only to be used for the purpose for which it has been
 * provided. No part of it is to be reproduced, disassembled, transmitted,
 * stored in a retrieval system nor translated in any human or computer
 * language in any way or for any other purposes whatsoever without the
 * prior written consent of HSBC Holdings plc.
 */
package org.intellij.vcs.mks;

import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CommandExecutionAdapter extends AbstractTableModel implements CommandExecutionListener {
	static class CommandStatistic {
		final
		@NotNull
		String command;
		long executionCount = 0;
		long totalExecutionTime = 0;

		CommandStatistic(@NotNull String command) {
			this.command = command;
		}

		public boolean equals(Object o) {
			return this == o ||
					!(o == null || getClass() != o.getClass()) && command.equals(((CommandStatistic) o).command);
		}

		public int hashCode() {
			return command.hashCode();
		}

		private void reset() {
			executionCount = 0;
			totalExecutionTime = 0;
		}

		public long averageInMs() {
			return (executionCount==0) ? 0 : totalExecutionTime / executionCount;
		}

		public long totalExecutionTimeInS() {
			return totalExecutionTime / 1000;
		}
	}

	final Map<String, CommandStatistic> statisticsByCommand = new HashMap<String, CommandStatistic>();
	final List<CommandStatistic> rows = new ArrayList<CommandStatistic>();
	private static final int NAME = 0;
	private static final int COUNT = 1;
	private static final int AVERAGE_TIME = 2;
	private static final int TOTAL_TIME = 3;
	private static final String[] COLUMN_TITLES = {MksBundle.message("performance_tab.column.title.command"),
			MksBundle.message("performance_tab.column.title.execution_count"),
			MksBundle.message("performance_tab.column.title.average.time.ms"),
			MksBundle.message("performance_tab.column.title.total.execution.time.s")};

	public int getRowCount() {
		return rows.size();
	}

	public int getColumnCount() {
		return 4;
	}

	public String getColumnName(int column) {
		return COLUMN_TITLES[column];
	}

	public synchronized void clear() {
		for (CommandStatistic statistic : rows) {
			statistic.reset();
		}
		fireTableDataChanged();
	}


	public Object getValueAt(int rowIndex, int columnIndex) {
		final CommandStatistic statistic = rows.get(rowIndex);
		switch (columnIndex) {
			case NAME:
				return statistic.command;
			case COUNT:
				return statistic.executionCount;
			case AVERAGE_TIME:
				return statistic.averageInMs();
			case TOTAL_TIME:
				return statistic.totalExecutionTimeInS();
			default:
				throw new IllegalArgumentException("unknown column index " + columnIndex);
		}
	}

	public synchronized void executionCompleted(String command, long duration) {
		CommandStatistic stat = statisticsByCommand.get(command);
		boolean dataChanged = false;
		if (stat == null) {
			stat = new CommandStatistic(command);
			statisticsByCommand.put(command, stat);
			rows.add(stat);
			dataChanged = true;
		}
		stat.totalExecutionTime += duration;
		stat.executionCount++;
		if (dataChanged) {
			fireTableDataChanged();
		} else {
			final int row = rows.indexOf(stat);
			fireTableRowsUpdated(row, row);
		}
	}
}
