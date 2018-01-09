This string of events/objects is manufactured, based on rmd18mddcw, but with a tombstone event added on the end

This directory contains a CREATE event, followed by two UPDATE events, followed by a tombstone event.



rmd18m7mj4: agent

rmd18m7msr: CREATE event (2015-07-24T19:35:06Z)
  source: n/a
  target: rmd18m7mr7  (adds one document for the <rmd18m7msr, rmd18m7mr7> tuple)
  agent:  rmd18m7mj4

-> target rmd18m7mr7: disco

<- source rmd18m7mr7: disco

rmd18mdd9v: UPDATE event (2015-07-29T17:40:24Z)
  source: rmd18m7mr7 (adds one document for the <rmd18mdd9v, rmd18m7mr7> tuple)
  target: rmd18mdd8b (adds one document for the <rmd18mdd9v, rmd18mdd8b> tuple)
  agent:  rmd18m7mj4

-> target rmd18mdd8b: disco

<- source rmd18mdd8b: disco

rmd18mdddd: UPDATE event (2015-07-29T17:47:18Z)
  source: rmd18mdd8b (adds one document for the <rmd18mdddd, rmd18mdd8b> tuple)
  target: rmd18mddcw (adds one document for the <rmd18mdddd, rmd18mddcw> tuple)
  agent:  rmd18m7mj4

-> target rmd18mddcw: disco

rmp1892fdx: TOMBSTONE event (2016-07-08T16:33:21.763Z)
  source: rmd18mddcw (adds one document for the <rmp1982fdx, rmd18mddcw> tuple)
  target: n/a
  agent:  rmd18m7mj4
