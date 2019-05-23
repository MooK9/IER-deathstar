// Agent stormtrooper in project jasonTeam.mas2j


@pfire[atomic]
+fire(X,Y)[source(firealarm)]
  <- !pos(X,Y);
     do(extinguish);
     -fire(X,Y)[source(_)];
     .send(firealarm,tell,extinguished(X,Y)).

+!pos(X,Y) : pos(X,Y)
  <- .print("Arrived to ", X, ", ", Y).

+!pos(X,Y) : not pos(X,Y)
  <- .print("going to ", X, ", ", Y);
     !next_step(X,Y);
     !pos(X,Y).

+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      do(D).

+!next_step(X,Y) : not pos(_,_)
   <- !next_step(X,Y).

-!next_step(X,Y) : true
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      !next_step(X,Y).

-fire(_,_) : not fire(_,_) & base5(X,Y)
  <- .print("Going home!");
     !pos(X,Y).


