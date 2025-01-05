package tt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class TimeBlock implements Comparable<TimeBlock> {
    private Date startTime;
    private Date endTime;
    private String task;

    public TimeBlock(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.task = null;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public long getDuration() {
        return endTime.getTime() - startTime.getTime();
    }

    @Override
    public int compareTo(TimeBlock o) {
        return this.startTime.compareTo(o.startTime);
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
        return format.format(startTime) + " - " + format.format(endTime);
    }
}

class TimeManagementSystem {
    private final List<TimeBlock> timeBlocks;

    public TimeManagementSystem() {
        timeBlocks = new ArrayList<>();
    }

    public void addTimeBlock(Date startTime, Date endTime) {
        if (endTime.before(startTime)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(endTime);
            cal.add(Calendar.DATE, 1);
            endTime = cal.getTime();
        }
        timeBlocks.add(new TimeBlock(startTime, endTime));
        insertionSortTimeBlocks();
    }

    public void updateTimeBlock(Date oldStartTime, Date oldEndTime, Date newStartTime, Date newEndTime) {
        if (newEndTime.before(newStartTime)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(newEndTime);
            cal.add(Calendar.DATE, 1);
            newEndTime = cal.getTime();
        }

        for (TimeBlock block : timeBlocks) {
            if (block.getStartTime().equals(oldStartTime) && block.getEndTime().equals(oldEndTime)) {
                block.setStartTime(newStartTime);
                block.setEndTime(newEndTime);
                insertionSortTimeBlocks();
                return;
            }
        }
    }

    public void deleteTimeBlock(Date startTime, Date endTime) {
        timeBlocks.removeIf(block -> block.getStartTime().equals(startTime) && block.getEndTime().equals(endTime));
    }

    public void clearAllTimeBlocks() {
        timeBlocks.clear();
    }

    public List<TimeBlock> getTimeBlocks() {
        return new ArrayList<>(timeBlocks);
    }

    public TimeBlock getLargestTimeBlock() {
        if (timeBlocks.isEmpty()) return null;
        TimeBlock largest = timeBlocks.get(0);
        for (TimeBlock block : timeBlocks) {
            if (block.getDuration() > largest.getDuration()) {
                largest = block;
            }
        }
        return largest;
    }

    public TimeBlock getSmallestTimeBlock() {
        if (timeBlocks.isEmpty()) return null;
        TimeBlock smallest = timeBlocks.get(0);
        for (TimeBlock block : timeBlocks) {
            if (block.getDuration() < smallest.getDuration()) {
                smallest = block;
            }
        }
        return smallest;
    }

    private void insertionSortTimeBlocks() {
        for (int i = 1; i < timeBlocks.size(); i++) {
            TimeBlock key = timeBlocks.get(i);
            int j = i - 1;
            while (j >= 0 && timeBlocks.get(j).compareTo(key) > 0) {
                timeBlocks.set(j + 1, timeBlocks.get(j));
                j--;
            }
            timeBlocks.set(j + 1, key);
        }
    }
}

public class TimeManagementUI extends JFrame {
    private final TimeManagementSystem system;
    private final JPanel timeBlocksPanel;
    private final JPanel summaryPanel;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
    private final Color[] blockColors = {Color.LIGHT_GRAY, Color.CYAN};
    private final Color summaryLightColor = Color.PINK;
    private final Color summaryDarkColor = Color.ORANGE;
    private final Color taskBackgroundColor = new Color(0xFFFACD); // Light yellow for task background
    private final Color taskBorderColor = new Color(0xFFD700); // Gold for border color
    private final Font taskFont = new Font("Arial", Font.BOLD, 16);

    public TimeManagementUI() {
        system = new TimeManagementSystem();
        timeBlocksPanel = new JPanel();
        timeBlocksPanel.setLayout(new BoxLayout(timeBlocksPanel, BoxLayout.Y_AXIS));
        JScrollPane timeBlocksScrollPane = new JScrollPane(timeBlocksPanel);

        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        JScrollPane summaryScrollPane = new JScrollPane(summaryPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, timeBlocksScrollPane, summaryScrollPane);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);

        add(splitPane);

        setTitle("Time Management System");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());

        JButton addTimeBlockButton = new JButton("Add Time Block");
        JButton clearAllButton = new JButton("Clear All");
       

        addTimeBlockButton.addActionListener(e -> addTimeBlock());
        clearAllButton.addActionListener(e -> {
            system.clearAllTimeBlocks();
            refreshTimeBlocksPanel();
            refreshSummaryPanel();
        });
       

        controlsPanel.add(addTimeBlockButton);
        controlsPanel.add(clearAllButton);
       
        add(controlsPanel, BorderLayout.NORTH);

        refreshTimeBlocksPanel();
        refreshSummaryPanel();
    }

    private void refreshTimeBlocksPanel() {
        timeBlocksPanel.removeAll();
        List<TimeBlock> blocks = system.getTimeBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            TimeBlock block = blocks.get(i);
            timeBlocksPanel.add(createTimeBlockPanel(block, blockColors[i % blockColors.length]));
        }

        timeBlocksPanel.revalidate();
        timeBlocksPanel.repaint();
    }

    private void refreshSummaryPanel() {
        summaryPanel.removeAll();
        TimeBlock largest = system.getLargestTimeBlock();
        TimeBlock smallest = system.getSmallestTimeBlock();

        if (largest != null) {
            summaryPanel.add(createSummaryBlock(largest, summaryLightColor, "Largest Time"));
        }
        if (smallest != null) {
            summaryPanel.add(createSummaryBlock(smallest, summaryDarkColor, "Smallest Time"));
        }

        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    private JPanel createSummaryBlock(TimeBlock block, Color color, String label) {
        JPanel blockPanel = new JPanel();
        blockPanel.setLayout(new BorderLayout());
        blockPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        blockPanel.setBackground(color);
        blockPanel.setPreferredSize(new Dimension(300, 100));

        // Displaying the time range and duration
        JLabel labelComponent = new JLabel(label + ": " + block.toString() + " - Duration: " + (block.getDuration() / (1000 * 60)) + " mins", SwingConstants.CENTER);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 16));
        labelComponent.setForeground(Color.BLACK);
        
        blockPanel.add(labelComponent, BorderLayout.NORTH);

        // Displaying the task
        String taskText = block.getTask() != null ? block.getTask() : "No Task Assigned";
        JLabel taskLabel = new JLabel("Task: " + taskText, SwingConstants.CENTER);
        taskLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        taskLabel.setForeground(Color.BLACK);
        blockPanel.add(taskLabel, BorderLayout.CENTER);

        return blockPanel;
    }


    private JPanel createTimeBlockPanel(TimeBlock block, Color color) {
        JPanel timeBlockPanel = new JPanel();
        timeBlockPanel.setBackground(color);
        timeBlockPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        timeBlockPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(color);
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel timeBlockLabel = new JLabel(block.toString());
        timeBlockLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(timeBlockLabel);
        timeBlockPanel.add(headerPanel, BorderLayout.NORTH);

        JLabel taskLabel = new JLabel(block.getTask() != null ? block.getTask() : "No Task");
        taskLabel.setFont(taskFont);
        taskLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taskLabel.setOpaque(true);
        taskLabel.setBackground(taskBackgroundColor);
        taskLabel.setBorder(BorderFactory.createLineBorder(taskBorderColor, 2)); // Gold border for task label
        timeBlockPanel.add(taskLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setOpaque(false);

        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton addTaskButton = new JButton("Add Task");
        JButton updateTaskButton = new JButton("Update Task");
        JButton deleteTaskButton = new JButton("Delete Task");

        updateButton.setPreferredSize(new Dimension(120, 30));
        deleteButton.setPreferredSize(new Dimension(120, 30));
        addTaskButton.setPreferredSize(new Dimension(120, 30));
        updateTaskButton.setPreferredSize(new Dimension(120, 30));
        deleteTaskButton.setPreferredSize(new Dimension(120, 30));

        updateButton.setBackground(new Color(0x007BFF));
        deleteButton.setBackground(new Color(0xDC3545));
        addTaskButton.setBackground(new Color(0x28A745));
        updateTaskButton.setBackground(new Color(0xFFC107));
        deleteTaskButton.setBackground(new Color(0x6C757D));

        updateButton.setForeground(Color.WHITE);
        deleteButton.setForeground(Color.WHITE);
        addTaskButton.setForeground(Color.WHITE);
        updateTaskButton.setForeground(Color.WHITE);
        deleteTaskButton.setForeground(Color.WHITE);

        updateButton.setToolTipText("Update the time block");
        deleteButton.setToolTipText("Delete the time block");
        addTaskButton.setToolTipText("Add a task to this time block");
        updateTaskButton.setToolTipText("Update the task in this time block");
        deleteTaskButton.setToolTipText("Delete the task in this time block");

        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(addTaskButton);
        buttonsPanel.add(updateTaskButton);
        buttonsPanel.add(deleteTaskButton);

        timeBlockPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addActionListeners(timeBlockPanel, block, updateButton, deleteButton, addTaskButton, updateTaskButton, deleteTaskButton);

        return timeBlockPanel;
    }

    private void addActionListeners(JPanel timeBlockPanel, TimeBlock block, JButton updateButton, JButton deleteButton, JButton addTaskButton, JButton updateTaskButton, JButton deleteTaskButton) {
        updateButton.addActionListener(e -> updateTimeBlock(block));
        deleteButton.addActionListener(e -> deleteTimeBlock(block));
        addTaskButton.addActionListener(e -> addTaskToTimeBlock(block));
        updateTaskButton.addActionListener(e -> updateTaskInTimeBlock(block));
        deleteTaskButton.addActionListener(e -> deleteTaskFromTimeBlock(block));
    }

    private void addTimeBlock() {
        JTextField startTimeField = new JTextField(10);
        JTextField endTimeField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Start Time (h:mm a):"));
        panel.add(startTimeField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("End Time (h:mm a):"));
        panel.add(endTimeField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Time Block", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Date startTime = timeFormat.parse(startTimeField.getText());
                Date endTime = timeFormat.parse(endTimeField.getText());
                system.addTimeBlock(startTime, endTime);
                refreshTimeBlocksPanel();
                refreshSummaryPanel();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use 'h:mm a'.");
            }
        }
    }

    private void updateTimeBlock(TimeBlock block) {
        JTextField newStartTimeField = new JTextField(10);
        JTextField newEndTimeField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("New Start Time (h:mm a):"));
        panel.add(newStartTimeField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("New End Time (h:mm a):"));
        panel.add(newEndTimeField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Update Time Block", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Date newStartTime = timeFormat.parse(newStartTimeField.getText());
                Date newEndTime = timeFormat.parse(newEndTimeField.getText());
                system.updateTimeBlock(block.getStartTime(), block.getEndTime(), newStartTime, newEndTime);
                refreshTimeBlocksPanel();
                refreshSummaryPanel();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid time format. Please use 'h:mm a'.");
            }
        }
    }

    private void deleteTimeBlock(TimeBlock block) {
        int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this time block?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            system.deleteTimeBlock(block.getStartTime(), block.getEndTime());
            refreshTimeBlocksPanel();
            refreshSummaryPanel();
        }
    }

    private void addTaskToTimeBlock(TimeBlock block) {
        if (block.getTask() == null) {
            String task = JOptionPane.showInputDialog("Enter task:");
            if (task != null && !task.trim().isEmpty()) {
                block.setTask(task);
                refreshTimeBlocksPanel();
                refreshSummaryPanel();
            }
        } else {
            JOptionPane.showMessageDialog(null, "This time block already has a task. Delete the existing task to add a new one.");
        }
    }

    private void updateTaskInTimeBlock(TimeBlock block) {
        if (block.getTask() != null) {
            String newTask = JOptionPane.showInputDialog("Update task:", block.getTask());
            if (newTask != null) {
                block.setTask(newTask);
                refreshTimeBlocksPanel();
                refreshSummaryPanel();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No task to update.");
        }
    }

    private void deleteTaskFromTimeBlock(TimeBlock block) {
        if (block.getTask() != null) {
            int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the task from this time block?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                block.setTask(null);
                refreshTimeBlocksPanel();
                refreshSummaryPanel();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No task to delete.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TimeManagementUI().setVisible(true));
    }
}                                     