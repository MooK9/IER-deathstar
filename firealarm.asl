// Agent firealarm in project jasonTeam.mas2j


!perceive(deathstar).

/* Plans */
@pfire[atomic]
+cell(X,Y,fire)
  <- do(siren_on);
     !change(color);
     .print("Fire at ", X, ", ", Y);
     .send(stormtrooper,tell,fire(X,Y)).

/*
+extinguished(X,Y)
  <- -cell(X,Y,fire);.
*/

+!change(color)
  :  cell(_,_,fire)
  <- .print("alarm changing color");
     do(change);
     .wait(500);
     !change(color).

@poff[atomic]
+!change(color)
  :  not cell(_,_,fire)
  <- do(siren_off);
     .print("No more fire!");.
     
+!perceive(deathstar)
  <- do(skip);
     .wait(100);
     !perceive(deathstar).


