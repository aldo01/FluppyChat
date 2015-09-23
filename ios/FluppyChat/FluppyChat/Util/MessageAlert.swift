//
//  MessageAlert.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/22/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

class MessageAlert {
    
    static func errorAlert( text : String ) {
        print( "ERROR: \(text)", "\n" )
    }
    
    static func showMessageForUser( text : String ) {
        UIAlertView(title: nil, message: text, delegate: nil, cancelButtonTitle: "Ok").show()
    }
}
