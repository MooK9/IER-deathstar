// Agent firealarm in project jasonTeam.mas2j


//!start.
//!change(color).

/* Plans */
@pcell1[atomic]
+cell(X,Y,fire)
  <- do(siren_on);
     !change(color);
     .print("Fire at ", X, ", ", Y);
     .send(stormtrooper,tell,fire(X,Y)).

@pcell2[atomic]
-cell(X,Y,fire)
  :  not cell(_,_,fire)
  <- do(siren_off);
     .print("No more fire!");.

+!change(color)
  <- .print("alarm changing color");
     do(change);
     .wait(500);
     !change(color).


