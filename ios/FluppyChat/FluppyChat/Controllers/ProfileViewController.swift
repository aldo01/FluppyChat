//
//  ProfileViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/2/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class ProfileViewController: UIViewController {
    @IBOutlet weak var userImage: UIImageView!

    override func viewDidLoad() {
        super.viewDidLoad()

        PhotoContainer.getImageForUser( PFUser.currentUser()! , imageView: userImage)
        userImage.layer.cornerRadius = userImage.frame.size.height / 2
        userImage.clipsToBounds = true
        userImage.contentMode = UIViewContentMode.ScaleAspectFill
    }
}
