import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileIORunner {

    public static final String CSV_FILE_NAME = "person.csv";
    public static final String JSON_FILE_NAME = "person.json";
    private static final String PATH = "src/main/java";

    private static String csvFilePath = PATH + CSV_FILE_NAME;
    private static String jsonFilePath = PATH + JSON_FILE_NAME;


    public static void main(String[] args) throws Exception {
        UserOutputService userOutputService = new SysoutUserOutputService();
        try (UserInputService userInputService = new ScannerUserInputService(userOutputService);) {
            ChooseOptionService chooseOptionService = new ChooseOptionService(userInputService);
            chooseOptionService.chooseMode();
            chooseOptionService.chooseOptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



        // this method reads person from csv file and create new Student object,
        // which is then put into the Singleton List
        public static void readFromFile(String fileName) {
            String returnString = new String();
            Scanner fileReader = null;
            try {
                File myFile = new File(fileName);
                fileReader = new Scanner(myFile);
                while (fileReader.hasNextLine()) {
                    returnString = fileReader.nextLine();
                    // process returns string into a set of variables
                    // split by comma
                    String[] personInfo = returnString.split(",");
                    // create a new student
                    // firstName, lastName, birthYear, birthMonth, birthDay
                    String firstName = personInfo[0];
                    String lastName= personInfo[1];
                    int birthYear= Integer.parseInt(personInfo[2]);
                    int birthMonth= Integer.parseInt(personInfo[3]);
                    int birthDay= Integer.parseInt(personInfo[4]);
                    Person person = new Person(firstName, lastName, birthYear, birthMonth, birthDay);
                    PeopleService.getInstance().addPerson(person);
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                if (fileReader != null)
                    fileReader.close();
            }
        }

        static void writeToFile(String fileName, String text) throws IOException {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(fileName);
                fileWriter.write(text);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                if (fileWriter != null)
                    fileWriter.close();
            }
        }

        static void writeJson(List<Person> personList) throws JsonProcessingException {
            String json = new ObjectMapper().writeValueAsString(personList);
            String newJson = "";
            for (String s : json.split("},")) {
                newJson += s + "}\n";
            }
            System.out.println("Formatted JSON:\n" + newJson);
        }

        static void saveAsJSON(String fileName, List<Person> people) {
            ObjectMapper mapper = new ObjectMapper();
            try{
                mapper.writeValue(new File(fileName), people);
            } catch(Exception e){
                System.out.println(e);
            }
        }


        static void savePersonList(List<Person> personList) throws IOException, Exception {
            StringBuffer allPersonsAsCSV = new StringBuffer();
            personList.forEach((person) -> {
                String personString = person.formatAsCSV();
                allPersonsAsCSV.append(personString + "\n");
            });
            writeToFile(csvFilePath, allPersonsAsCSV.toString());
            writeJson(personList);
        }
    }


interface UserInputService extends AutoCloseable {
    String getUserInput(String prompt);
}
interface UserOutputService {
    void printMessage(String message);
}

class SysoutUserOutputService implements UserOutputService {
    @Override
    public void printMessage(String message) {
        System.out.println(message);
    }
}

class ScannerUserInputService implements UserInputService {
    private Scanner scanner;
    private UserOutputService userOutputService;
    public ScannerUserInputService(UserOutputService userOutputService) {
        this.scanner = new Scanner(System.in);
        this.userOutputService = userOutputService;
    }
    public String getUserInput(String prompt) {
        userOutputService.printMessage(prompt);
        String input = scanner.nextLine();
        if (input.isBlank()) {
            return getUserInput(prompt);
        }
        return input;
    }
    @Override
    public void close() throws Exception {
        scanner.close();
    }
}

class PersonBuilderService {
    private UserInputService userInputService;
    public PersonBuilderService(UserInputService userInputService) {
        this.userInputService = userInputService;
    }
    public Person createPerson() {
        String firstName = userInputService.getUserInput("What's the person's first name?");
        String lastName = userInputService.getUserInput("What's the person's last name?");
        int birthYear = Integer.parseInt(userInputService.getUserInput("What's the person's year of birth?"));
        int birthMonth = Integer.parseInt(userInputService.getUserInput("What's the person's month of birth?"));
        int birthDay = Integer.parseInt(userInputService.getUserInput("What's the person's day of birth?"));
        Person person = new Person(firstName, lastName, birthYear, birthMonth, birthDay);
        PeopleService.getInstance().addPerson(person);
        return person;
    }

    static void serializeObject(Object o) throws IOException {
        ObjectOutputStream objectStream = null;
        try {
            FileOutputStream objFile = new FileOutputStream("person.dat");
            objectStream = new ObjectOutputStream(objFile);
            objectStream.writeObject(o);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (objectStream != null) {
                objectStream.flush();
                objectStream.close();
            }
        }
    }

    static Object deserializeObject() throws IOException {
        ObjectInputStream personInputStream = null;
        Object newObject = null;
        try {
            FileInputStream personFile = new FileInputStream("person.dat");
            personInputStream = new ObjectInputStream(personFile);
            newObject = personInputStream.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            if (personInputStream != null) {
                personInputStream.close();
            }
        }
        return newObject;
    }
}

class PeopleService {
    private List<Person> people;
    private PeopleService() {
        this.people = new ArrayList<>();
    }
    private static PeopleService singleton;
    public static PeopleService getInstance() {
        if (singleton == null) {
            singleton = new PeopleService();
        }
        return singleton;
    }
    public void addPerson(Person p) {
        people.add(p);
    }
    public List<Person> getPeople() {
        return people;
    }
    @Override
    public String toString() {
        return people.toString();
    }
}

class ChooseOptionService {
    private UserInputService userInputService;
    public ChooseOptionService(UserInputService userInputService) {
        this.userInputService = userInputService;
    }

    public void chooseMode() throws IOException {
        String option = userInputService.getUserInput("Enter '1' to restore a file or '2' to start new.");
        if (option.equals("1")) {
            // checking if the file exists; if so, read it
            if (new File(FileIORunner.CSV_FILE_NAME).exists())
                FileIORunner.readFromFile(FileIORunner.CSV_FILE_NAME);
        } else if (option.equals("2")) {
            try {
                chooseOptions();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            System.exit(0);
        }
    }


    public void chooseOptions() throws IOException {
        boolean processing = true;
        while (processing) {
            String option = userInputService.getUserInput("Type 'a' to add a person to the list, 'p' to print a list of current people, 's' to serialize a person, 'd' to deserialize a person, or simply press anything else to exit the program");
            if (option.equals("a")) {
                PersonBuilderService personBuilderService = new PersonBuilderService(userInputService);
                personBuilderService.createPerson();
            } else if (option.equals("p")) {
                System.out.println(PeopleService.getInstance().getPeople());
            } else if (option.equals("s")) {
                if (!PeopleService.getInstance().getPeople().isEmpty()) {
                    for (Person person: PeopleService.getInstance().getPeople()) {
                        try {
                            PersonBuilderService.serializeObject(person);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            } else if (option.equals("d")) {
                Object deserializedStudent = PersonBuilderService.deserializeObject();
                System.out.println(deserializedStudent.toString());
            } else {
                StringBuilder peopleCSV = new StringBuilder();
                // for each person in PeopleService,
                for (Person person: PeopleService.getInstance().getPeople()) {
                    // format each person as csv
                    String personString = person.formatAsCSV();
                    // append it to peopleCSV and \n
                    peopleCSV.append(personString + "\n");
                }
                try {
                    String exportOptions = userInputService.getUserInput("Enter 1 to export as CSV || Enter 2 to export as JSON || ENTER 3 to export as both");
                    switch (Integer.parseInt(exportOptions)) {
                        case 1:
                            FileIORunner.writeToFile(FileIORunner.CSV_FILE_NAME, peopleCSV.toString());
                            break;
                        case 2:
                            FileIORunner.saveAsJSON(FileIORunner.JSON_FILE_NAME, PeopleService.getInstance().getPeople());
                            break;
                        case 3:
                            FileIORunner.writeToFile(FileIORunner.CSV_FILE_NAME, peopleCSV.toString());
                            FileIORunner.saveAsJSON(FileIORunner.JSON_FILE_NAME, PeopleService.getInstance().getPeople());
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Something went wrong");
                } finally {
                    System.out.println("Program ended");
                    processing = false;
                    System.exit(0);
                }

            }
        }

    }
}
