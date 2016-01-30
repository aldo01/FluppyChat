//
//  AcceptDeclineRoomProtocol.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 1/30/16.
//  Copyright © 2016 Dmytro Bohachevskyy. All rights reserved.
//

import Foundation

protocol AcceptDeclineRoomProtocol {
    
    func acceptRoom( room : PFObject )
    
    func declineRoom( room : PFObject )
    
}