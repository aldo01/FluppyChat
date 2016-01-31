//
//  ProfileViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/2/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class ProfileViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    @IBOutlet weak var userImage: UIImageView!
    @IBOutlet weak var userName: UILabel!
    
    let imagePicker = UIImagePickerController()
    var delegate : UpdateRoomListProtocol?
    var imageWasChanged : Bool = false

    override func viewDidLoad() {
        super.viewDidLoad()

        PhotoContainer.getImageForUser( PFUser.currentUser()! , imageView: userImage)
        userImage.layer.cornerRadius = userImage.frame.size.height / 2
        userImage.clipsToBounds = true
        userImage.contentMode = UIViewContentMode.ScaleAspectFill
        userName.text = PFUser.currentUser()!.username!
        
        // add gesture recognizer to the photo
        userImage.userInteractionEnabled = true
        userImage.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "chosePicture"))
    }
    
    // select image from gllery
    // called when user click to photo image view
    func chosePicture() {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.SavedPhotosAlbum){
            print("Button capture")
            
            imagePicker.delegate = self
            imagePicker.sourceType = UIImagePickerControllerSourceType.SavedPhotosAlbum;
            imagePicker.allowsEditing = false
            
            self.presentViewController(imagePicker, animated: true, completion: nil)
        }
    }
    
    // receive image
    func imagePickerController(picker: UIImagePickerController!, didFinishPickingImage image: UIImage!, editingInfo: NSDictionary!){
        self.dismissViewControllerAnimated(true, completion: nil)
        userImage.image = image
        
        let user = PFUser.currentUser()!
        PhotoContainer.replacePhotoForUser(user, image: image)
        
        // upload image to the parse
        let file = PFFile(name: "PHOTO_" + (user.objectId!), data: UIImageJPEGRepresentation(image, CGFloat(0.5))!)
        file.saveInBackground()
        user["profilepic"] = file
        user.saveInBackground()
        
        imageWasChanged = true
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if nil != delegate {
            delegate?.updateFriendsList(imageWasChanged)
        }
    }
}
