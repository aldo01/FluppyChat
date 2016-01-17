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
    let KEYBOARD_SIZE : CGFloat = 236
    
    
    @IBOutlet weak var loginTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: "DismissKeyboard")
        view.addGestureRecognizer(tap)
    }
    
    //Calls this function when the tap is recognized.
    func DismissKeyboard(){
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
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
                MessageAlert.errorAlert(error?.description ?? "")
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
