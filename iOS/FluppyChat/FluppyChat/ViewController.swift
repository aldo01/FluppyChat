//
//  ViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 20.02.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class ViewController: UIViewController {

    @IBOutlet weak var loginTextField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        if (( PFUser.currentUser() ) != nil) {
        //    makeLogin()
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    private func makeLogin() {
        print("make login\n")
        self.performSegueWithIdentifier("loginSuccess", sender: self)
    }
    
    @IBAction func loginAction() {
        let user : PFUser = PFUser()
        
        user.username = loginTextField.text
        user.password = passwordField.text
        
        PFUser.logInWithUsernameInBackground(loginTextField.text, password: passwordField.text) { (user:PFUser!, err:NSError!) -> Void in
            if ( nil == err ) {
                self.makeLogin()
            } else {
                // display error
                print(err.description)
                UIAlertView(title: nil, message: err.description, delegate: self, cancelButtonTitle: "Ok").show()
            }
        
        }
    }

}

