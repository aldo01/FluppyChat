//
//  LoginViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/22/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//
//  Perform sign in and sign up action
//
//  TODO:
//  1. Check user enter
//  2. Hide keyboard when user touch view
//

import UIKit

class LoginViewController: UIViewController {
    @IBOutlet weak var loginTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        // Do any additional setup after loading the view.
        if (PFUser.currentUser() != nil) {
            performLoginAction()
        }
    }

    @IBAction func signInAction(sender: AnyObject) {
        let username = loginTextField.text!
        let password = passwordTextField.text!
    
        // perform login action
        PFUser.logInWithUsernameInBackground(username, password : password) {
            (user: PFUser?, error: NSError?) -> Void in
            if user != nil {
                // show room list
                self.performLoginAction()
            } else {
                // motify user about error
                MessageAlert.errorAlert("Login failed")
                MessageAlert.showMessageForUser("Incorrect username or password")
            }
        }
    }
    
    @IBAction func signUpAction(sender: AnyObject) {
    }
    
    private func performLoginAction() {
        self.performSegueWithIdentifier("LoginController2RoomController", sender: self)
    }
}
