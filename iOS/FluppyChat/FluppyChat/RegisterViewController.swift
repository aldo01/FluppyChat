//
//  RegisterViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 20.02.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class RegisterViewController: UIViewController {
    
    @IBOutlet weak var loginField: UITextField!
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    @IBAction func registerAction() {
        let user = PFUser()
        user.username = loginField.text
        user.email = emailField.text
        user.password = passwordField.text
        
        user.signUpInBackgroundWithBlock({ (res :Bool, err:NSError!) -> Void in
            if ( ( err ) != nil) {
                print(err)
            } else {
                self.navigationController?.popToRootViewControllerAnimated(true)
            }
        })
    
    }

}
