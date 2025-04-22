package com.editorbackend.editorbackend;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;



@Service
public class Main {
     @PostConstruct
    public void initTest(){
        new Thread(this::test).start();
        System.out.println("Starting test app");

    }

    public void test() {
        CRDTTree crdtTree = new CRDTTree();
        Scanner scanner = new Scanner(System.in);
          List<User> userList = new ArrayList<>();
    System.out.println("Enter the number of users to create:");
    int userCount = Integer.parseInt(scanner.nextLine());

    for (int i = 0; i < userCount; i++) {
        System.out.println("Enter details for User " + (i + 1) + ":");
        System.out.print("User ID: ");
        String userID = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = new User(userID, email, password);
        userList.add(user);
    }

        
        while (true) {
            crdtTree.printCRDTTree();
            crdtTree.printText();
            System.out.println("\nChoose: 1.Insert 2.Delete 3.Undo 4.Redo 5.Exit");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1: //insert
                    System.out.print("Enter char: ");
                    char val = scanner.nextLine().charAt(0);
                    System.out.print("Enter index: ");
                    int idx = Integer.parseInt(scanner.nextLine());
                    System.out.print("User ID: ");
                    String uid = scanner.nextLine();

                    User u = userList.stream().filter(x -> x.getUserID().equals(uid)).findFirst().orElse(null);
                    if (u == null) { System.out.println("User not found"); break; }

                    CRDTNode node = crdtTree.insert(val, idx, LocalDateTime.now(), uid);
                    u.addToUndoStack("insert", node, idx);
                    break;

                case 2: //delete
                    System.out.print("Enter index: ");
                    int delIdx = Integer.parseInt(scanner.nextLine());
                    System.out.print("User ID: ");
                    String delUid = scanner.nextLine();

                    User delUser = userList.stream().filter(x -> x.getUserID().equals(delUid)).findFirst().orElse(null);
                    if (delUser == null) { System.out.println("User not found"); break; }

                    CRDTNode delNode = crdtTree.visibleNodes.get(delIdx);
                    crdtTree.delete(delIdx, delUid);
                    delUser.addToUndoStack("delete", delNode, delIdx);
                    break;

                case 3: // undo
                    System.out.print("User ID: ");
                    String undoUid = scanner.nextLine();
                    User undoUser = userList.stream().filter(x -> x.getUserID().equals(undoUid)).findFirst().orElse(null);
                    if (undoUser == null) { System.out.println("User not found"); break; }

                    Operation op = undoUser.undo(crdtTree);
                    if (op != null)
                        System.out.println("Undid " + op.getType() + " on node " + op.getNode().getId());
                    break;
                case 4: //redo 
                    System.out.print("User ID: ");
                    String redoUid = scanner.nextLine();
                    User redoUser = userList.stream().filter(x -> x.getUserID().equals(redoUid)).findFirst().orElse(null); 
                    if (redoUser == null) { System.out.println("User not found"); break; }

                    Operation redoOp = redoUser.redo(crdtTree);
                    if (redoOp != null)
                    System.out.println("Undid " + redoOp.getType() + " on node " + redoOp.getNode().getId());
                    break; 
                case 5:
                    scanner.close();
                    return;
            }
        }
    }

    
}
