package entities;
import tools.Input;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static entities.Task.sortByStartDate;
import static java.time.temporal.ChronoUnit.DAYS;

public class ProjectLibrary extends DataLibrary{
    private static final ProjectLibrary instance = null;
    Input input = Input.getInstance();
    
    public static ProjectLibrary getInstance() {
        if (instance == null) {
            return new ProjectLibrary();
        } else {
            return instance;
        }
    }
    
    public boolean confirmAccess(Team projectTeam, User currentUser) {
        if (projectTeam.findTeamMember(currentUser).getRole().adminAccess()) {
            return true;
        } else {
            System.out.println("You are not authorized to perform this action!");
            return false;
        }
    }
    
    public boolean noTeam(Project currentProject) {
        if(currentProject.getTeam() == null) { //THIS SHOULD BE REMOVED AS TEAMS SHOULD BE CREATED AUTOMATICALLY.
            System.out.println("No team created in current project.");
            return true;
        }
        return false;
    }
    
    public void createProject(User currentUser) {
        Input input = Input.getInstance();
        String name = input.getStr("Project name: ");
        String description = input.getStr("Project description: ");
        LocalDate startDate = input.getDate("Project start date (YYYY-MM-DD): ");
        LocalDate endDate = input.getDate("Project end date (YYYY-MM-DD): ");
        addToList(new Project(name, currentUser, description, startDate, endDate));
    }
    
    public void addProjectToList(Project project)
    {
        list.add(project);
    }
    
    public void addBudgetToList(Project currentProject, double money, double hours) {
        currentProject.getBudget().setMoney(money);
        currentProject.getBudget().setHours(hours);
    }
    
    public ArrayList<Project> listUsersProjects(User currentUser){
        ArrayList<Project> tempList = new ArrayList<>();
        for(Data project : list){
            Project currentProject = ((Project)project);
            if ((currentProject.getTeam() == null &&
                    currentProject.getProjectManager().getUserName().equals(currentUser.getUserName()))
                    ||
                    (currentProject.getTeam() != null &&
                            currentProject.getTeam().findTeamMember(currentUser) != null)) {
                
                tempList.add(currentProject);
            }
        }
        
        if (tempList.size() == 0){
            System.out.println("You have no projects!");
        } else {
            for (int i=0; i<tempList.size(); i++){
                System.out.println(" " + (i + 1) + ") " + tempList.get(i).getName());
            }
        }
        
        return tempList;
    }
    
    public Project navigateBetweenProjects(User currentUser){
        ArrayList<Project> projectList = listUsersProjects(currentUser);
        if(projectList.size()==0){
            return null;
        } else {
            int choice;
            do{
                choice = input.getInt("Enter project number or 0 to return to the previous menu: ");
            } while(choice < 0 || choice > projectList.size());
            
            if (choice == 0){
                return null;
            } else
                return projectList.get(choice-1);
        }
    }
    
    public void updateName(User currentUser){
        Project currentProject = null;
        for(Data project : list) {
            currentProject = ((Project) project);
        }
        if(currentProject.getTeam().findTeamMember(currentUser).getRole().adminAccess()){
            String newDesc = input.getStr("Enter the description: ");
            currentProject.setName(newDesc);
        }
        else{
            System.out.println("You are not authorized to perform this action!!");
        }
        
    }
    
    public void viewProjectDetails(Project currentProject){
        if(findItInList(currentProject.getID()) != null){
            System.out.println("Project Name: " + currentProject.getName());
            System.out.println("Project Description: " + currentProject.getDescription());
            if(!currentProject.getStatus().isEmpty()){
                System.out.println("Status: "+ currentProject.getStatus());
            }
            System.out.println("Start Date: " + currentProject.getStartDate());
            System.out.println("End Date: " + currentProject.getEndDate());
            taskVisualisation(currentProject);
        } else {
            System.out.println("Project does not exist!");
        }
        
    }
    
    public void updateStatus(Project currentProject, User currentUser){
        if(currentProject.getProjectManager().checkID(currentUser.getID())){
            String newStatus = input.getStr("Enter the status: ");
            if(confirmAction("Are you sure you want to update the status?")){
                currentProject.setStatus(newStatus);
            }
        }
        else{
            System.out.println("You are not authorized to perform this action!");
        }
    }
    
    
    public boolean deleteProject(Project currentProject, User currentUser){
        Project projectToDelete = (Project)findItInList(currentProject.getID());
        if(projectToDelete==null){
            System.out.println("Project does not exist!");
            return false;
        }
        if(projectToDelete.getProjectManager().checkID(currentUser.getID())){
            System.out.println("Warning!");
            System.out.println("You are about to delete the entire project!");
            System.out.println("This action is irreversible!");
            String password = input.getStr("Please enter password to confirm the deletion (leave empty to abort): ");
            if(currentUser.getPassword().equals(password)){
                if(removeItFromList(projectToDelete.getID())){
                    System.out.println("Project successfully deleted!");
                    System.out.println("Returning to main menu...");
                    return true;
                } else {
                    System.out.println("Something went wrong");
                    return false;
                }
            } else if(password.isEmpty()){
                System.out.println("Action aborted!");
                System.out.println("Returning to main menu...");
                return false;
            } else {
                System.out.println("Confirmation Failed!");
                return false;
            }
        }
        System.out.println("You are not authorized to perform this action!");
        return false;
    }
    
    private boolean confirmAction(String text){
        Input input = Input.getInstance();
        System.out.println(text);
        boolean confirmCheck = false;
        while(!confirmCheck){
            String confirmation = input.getStr("Please enter yes or no: ");
            if(confirmation.equalsIgnoreCase("yes") || confirmation.equalsIgnoreCase("y")){
                return true;
            } else if (confirmation.equalsIgnoreCase("no") || confirmation.equalsIgnoreCase("n")) {
                System.out.println("Action aborted!");
                return false;
            } else {
                System.out.println("Invalid input!");
            }
        } return false;
        
    }
    
    public void viewCost(Project currentProject)
    {
        if(currentProject==null){
            System.out.println("Project does not exist!");
            return;
        }
        TaskLibrary taskLibrary = currentProject.taskList;
        getTotalCostProject(taskLibrary, true);
    }
    
    public double getTotalCostProject(TaskLibrary taskLibrary, boolean print) {
        double totalCostProject = 0;
        for (Data task : taskLibrary.list)
        {
            Task currentTask = (Task) task;
            totalCostProject += getTotalCostTask(currentTask, print);
        }
        if(print) {
            System.out.println("Total cost for the project is " + totalCostProject + "kr.");
        }
        return totalCostProject;
    }
    
    public double getTotalCostTask(Task currentTask, boolean print) {
        double totalCostPerTask = 0;
        for (WorkedHours log : currentTask.getWorkedHours())
        {
            totalCostPerTask += log.getUser().getSalary() * log.getWorkedHours();
        }
        if(print) {
            System.out.println("The total cost for the Task: " + currentTask.getName() + " " + totalCostPerTask + "kr.");
        }
        return totalCostPerTask;
    }
    
    public void getExtraDeveloperSuggestion(Project currentProject, User currentUser) {
        double totalCostPerTaskUser = 0;
        if(noTeam(currentProject)) {
            return;
        }
        if(!confirmAccess(currentProject.getTeam(),currentUser)) {
            return;
        }
        if(getOnBudgetMoney(currentProject) && getOnBudgetHours(currentProject)) {
            double budgetMoney = currentProject.getBudget().getMoney();
            double totalCost = getTotalCostProject(currentProject.taskList, false);
            double projectDuration = currentProject.duration();
            double costPerDay = totalCost / projectDuration;
            double daysRemaining = getBudgetHoursRemaining(currentProject) / 24;
            double iteratedCostRemainingDays = daysRemaining * costPerDay;
            double iteratedRemainingMoney = budgetMoney - (totalCost + iteratedCostRemainingDays);
            double iteratedMoneyToUsePerDay = iteratedRemainingMoney / daysRemaining;
            double iteratedMoneyToUsePerHour = iteratedMoneyToUsePerDay / 8;
            
            double developerRate = 100.0;
            
            
        }
    }
    
    public void viewBudget(Project currentProject, User currentUser) {
        if(noTeam(currentProject)) {
            return;
        }
        if(!confirmAccess(currentProject.getTeam(),currentUser)) {
            return;
        }
        double budgetMoney = currentProject.getBudget().getMoney();
        double budgetHours = currentProject.getBudget().getHours();
        double hoursRemaining = getBudgetHoursRemaining(currentProject);
        double daysRemaining = hoursRemaining / 24;
        double remainingMoney = getBudgetMoneyRemaining(currentProject);
        String informBudgetReachedSEK = "";
        String informBudgetReachedHours = "";
        if(!getOnBudgetMoney(currentProject)) {
            informBudgetReachedSEK = "\nYou have reached your budget limit in SEK!";
        }
        if(!getOnBudgetHours(currentProject)) {
            informBudgetReachedHours = "\nYou have reached your budget limit in Hours!";
        }
        System.out.println("Budget Statistics: " +
                "\nTotal SEK: " + budgetMoney +
                "\nTotal Hours: " + budgetHours +
                "\nDays remaining on budget: " + daysRemaining +
                "\nHours remaining on budget: " + hoursRemaining +
                informBudgetReachedHours +
                "\nSEK remaining on budget: " + remainingMoney +
                informBudgetReachedSEK);
    }
    
    public double getBudgetMoneyRemaining(Project currentProject) {
        return currentProject.getBudget().getMoney() - getTotalCostProject(currentProject.taskList, false);
    }
    
    public double getBudgetHoursRemaining(Project currentProject) {
        return currentProject.getBudget().getHours() - getHoursWorkedProject(currentProject);
    }
    
    public boolean getOnBudgetMoney(Project currentProject) {
        double budgetSEK = currentProject.getBudget().getMoney();
        double currentCost = getTotalCostProject(currentProject.taskList, false);
        if(budgetSEK > currentCost) {
            return true;
        }
        return false;
    }
    
    public boolean getOnBudgetHours(Project currentProject) {
        double budgetHours = currentProject.getBudget().getHours();
        double currentHours = getHoursWorkedProject(currentProject);
        if(budgetHours > currentHours) {
            return true;
        }
        return false;
    }
    
    public double getHoursWorkedProject(Project currentProject) {
        double totalHoursProject = 0.0;
        for (Data task : currentProject.taskList.list) {
            Task currentTask = (Task) task;
            totalHoursProject += getHoursWorkedTask(currentTask);
        }
        return totalHoursProject;
    }
    
    public double getHoursWorkedTask(Task currentTask) {
        double totalHoursTask = 0;
        for (WorkedHours log : currentTask.getWorkedHours()) {
            totalHoursTask += log.getWorkedHours();
        }
        return totalHoursTask;
    }
    
    public void addBudgetMoney(Project currentProject, User currentUser) {
        if(noTeam(currentProject)) {
            return;
        }
        if(currentProject.getBudget().budgetExistMoney()) {
            updateBudgetMoney(currentProject, currentUser);
            return;
        }
        if (!confirmAccess(currentProject.getTeam(), currentUser)) {
            return;
        }
        double value = -1;
        do {
            value = input.getDouble("What is the total budget value in SEK?");
        } while(value < 0);
        currentProject.getBudget().setMoney(value);
        System.out.println("Budget in SEK has been added");
    }
    
    public void addBudgetHours(Project currentProject, User currentUser) {
        if(noTeam(currentProject)) {
            return;
        }
        if(currentProject.getBudget().budgetExistHours()) {
            updateBudgetHours(currentProject, currentUser);
            return;
        }
        if (!confirmAccess(currentProject.getTeam(), currentUser)) {
            return;
        }
        double value = -1;
        do {
            value = input.getDouble("What is the total budget in hours?");
        } while(value < 0);
        currentProject.getBudget().setMoney(value);
        System.out.println("Budget in hours has been added");
    }
    
    public void updateBudgetMoney(Project currentProject, User currentUser) {
        if(noTeam(currentProject)) {
            return;
        }
        if(!confirmAccess(currentProject.getTeam(), currentUser)) {
            return;
        }
        double value = -1;
        do {
            value = input.getDouble("What is the total budget value in SEK?");
        } while(value < 0);
        currentProject.getBudget().setMoney(value);
        System.out.println("Budget in SEK has been updated");
    }
    
    public void updateBudgetHours(Project currentProject, User currentUser) {
        if(noTeam(currentProject)) {
            return;
        }
        if(!confirmAccess(currentProject.getTeam(), currentUser)) {
            return;
        }
        double value = -1;
        do {
            value = input.getDouble("What is the total budget in hours?");
        } while(value < 0);
        currentProject.getBudget().setMoney(value);
        System.out.println("Budget in hours has been updated");
    }
    
    public void taskVisualisation(Project currentProject) {
        ArrayList<Data> taskList = currentProject.taskList.list;
        Collections.sort(taskList, sortByStartDate);
        LocalDate startDate = currentProject.getStartDate();
        LocalDate endDate = currentProject.getEndDate();
        long daysOfProject = DAYS.between(startDate, endDate);
        long amountOfDates = daysOfProject/20 + 1;
        LocalDate printedDate = startDate;
        System.out.println("\n" + input.PURPLE + "GANTT CHART" + input.RESET);
        System.out.print("                    ");
        for (int i = 1; i < amountOfDates ; i++){
            if (DAYS.between(printedDate, endDate) > 19){
                System.out.print(printedDate + "          ");
                printedDate = printedDate.plusDays(20);
            }
        }
        System.out.println(endDate);
        for (Data task : taskList){
            Task currentTask = (Task) task;
            int characters = charactersInName(currentTask);
            System.out.print(currentTask.getName());
            for (int i = characters; i < 20; i++) System.out.print(" ");
            long daysFromStart = DAYS.between(startDate, currentTask.getStartDate());
            for (int i = 0; i < daysFromStart; i++) System.out.print(" ");
            long durationOfTask = DAYS.between(currentTask.getStartDate(), currentTask.getDeadline());
            for (int i = 0; i < durationOfTask; i++){
                System.out.print(input.BLUE + "¤");
            }
            System.out.println(input.RESET + "\n");
        }
    }
    
    public int charactersInName(Task task){
        String name = task.getName();
        int count = name.length();
        return count;
    }
    
}
