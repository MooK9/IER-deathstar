// Agent firealarm in project jasonTeam.mas2j


!start.
!alarm(fire).
!change(color).

/* Plans */
+cell(x,y,fire)
<- !change(color);
.print("Fire at ", X, ", ", Y);	
     .send(stormtrooper,tell,fire(X,Y));.

+!change(color)
  <- do(change);
     .wait(500);
     !!change(color).



