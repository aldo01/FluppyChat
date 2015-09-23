//
//  PhotoContainer.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//
//  This class contain the user images.
//  Called when app need some photo.
//  If photo there is in archive - return,
//  else download him
//

class PhotoContainer {
    static var photosDic : [ PFUser : UIImage ]!
    
    func getImageForUser( user : PFUser, imageView : UIImageView ) {
        if ( nil != PhotoContainer.photosDic[user] ) {
            imageView.image = PhotoContainer.photosDic[user]!
        } else {
            // download photo in background
            let file = user["profilepic"] as! PFFile
            file.getDataInBackgroundWithBlock({ ( image : NSData?, err : NSError?) -> Void in
                if ( nil == err ) {
                    let image = UIImage(data: image!)
                    
                    // add image to the dictionary
                    PhotoContainer.photosDic[user] = image
                    
                    imageView.image = image
                }
            })
        }
    }
    
}
