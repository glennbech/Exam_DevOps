# Eksamen PGR301 2023

# Oppgave 1. Kjell's Python kode

## A. SAM & GitHub actions workflow

Koden er skrevet som en AWS SAM applikasjon, og ligger i mappen "kjell" i dette repoet. Det er åpenbart at Kjell har
tatt utgangspunkt i et "Hello World" SAM prosjekt og bare brukt navnet sitt som applikasjonsnavn.

* Denne SAM-applikasjonen oppretter en S3 Bucket og du bør sørge for at den lages med ditt kandidatnavn, og du kan under eksamen bruke
  denne bucketen til å laste opp egne bilder for å teste din egen applikasjon.
* I ditt Cloud9-miljø, eller på din egen maskin, kan du bygge og deploye koden til AWS ved å bruke ```sam cli```
* Det anbefales å teste dette før du fortsetter.

Advarsel! Se opp for hardkoding ! Du må kanskje endre noe for å få deployet selv.

### Oppgave

* Fjerne hardkoding  av S3 bucket navnet ```app.py koden```, slik at den leser verdien "BUCKET_NAME" fra en miljøvariabel.
* Du kan gjerne teste APIet ditt ved å bruke kjell sine bilder  https://s3.console.aws.amazon.com/s3/buckets/kjellsimagebucket?region=eu-west-1
* Du skal opprette en GitHub Actions-arbeidsflyt for SAM applikasjonen. For hver push til main branch, skal
  arbeidsflyten bygge og deploye Lambda-funksjonen.
* Som respons på en push til en annen branch en main, skal applikasjonen kun bygges.
* Sensor vil lage en fork av ditt repository. Forklar hva sensor må gjøre for å få GitHub Actions workflow til å kjøre i
  sin egen GitHub-konto.

## Getting Actions to run
* To make the actions run properly you have to add IAM Keys to github secrets with the following format:
* Name: AWS_ACCESS_KEY_ID and then the key id as the secret
* Name: AWS_SECRET_ACCESS_KEY and then the secret key as the secret

## B. Docker container

Python er ikke et veldig etablert språk i VerneVokterene, og du vil gjerne at utviklere som ikke har Python
installert på sin maskin skal kunne teste koden.

### Opppgave

Lag en Dockerfile som bygger et container image du kan bruke for å kjøre python koden.

Dockerfilen skal lages i mappen ```/kjell/hello_world```.

* To make app.py run properly you will have to add a .env file in the root directory containing the line BUCKET_NAME= (insert the name of your bucket here)
* You will also have to run the following command while in the kjell/hello_world directory to have docker run the correct dockerfile

Run the command 
```shell
docker build -t kjellpy . 
docker run -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket kjellpy
```

# Oppgave 2. Overgang til Java og Spring boot

Du innser raskt at Python ikke er veien videre for et konkurransedyktig produkt og har selv laget starten på en
Java-applikasjon som ligger i dette repoet. Applikasjonen er en Spring Boot applikasjon, som eksponerer et endepunkt

```http://<host>:<port>/scan-ppe?bucketName=<bucketnavn>```

## A. Dockerfile

* Test java-applikasjonen lokalt i ditt cloud9 miljø ved å stå i rotmappen til ditt repository, og kjøre
  kommandoen ```mvn spring-boot:run```
* Du kan teste applikasjonen i en terminal med ```curl localhost:8080/scan-ppe?bucketName=<din bucket>``` og se på
  responsen.

### Oppgave

* Lag en Dockerfile for Java-appliksjonen. Du skal lage en multi stage Dockerfile som både kompilerer og kjører
  applikasjonen.

Sensor vil lage en fork av ditt repository, og skal kunne kjøre følgende kommandoer for å starte en docker container

```shell
docker build -t ppe . 
docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket ppe
```

## B. GitHub Actions workflow for container image og ECR

Du skal nå automatisere prosessen med å bygge/kompilere og teste Java-applikasjonen.
Lag en ny GitHub Actions Workflow fil, ikke gjenbruk den du lagde for Pythonkoden.

### Oppgave

* Lag en GitHub actions workflow som ved hver push til main branch lager og publiserer et nytt Container image til et
  ECR repository.
* Workflow skal kompilere og bygge et nytt container image, men ikke publisere image til ECR dersom branch er noe annet en main.
* Du må selv lage et ECR repository i AWS miljøet, du trenger ikke automatisere prosessen med å lage
  dette.
* Container image skal ha en tag som er lik commit-hash i Git, for eksempel: ```glenn-ppe:b2572585e```.
* Den siste versjonen av container image som blir pushet til ECR, skal i tillegg få en tag "latest".


# Oppgave 3- Terraform, AWS Apprunner og Infrastruktur som kode

Se på koden som ligger i infra katalogen, den inneholder kun en app_runner_service og en IAM roller som gjør denne i
stand til å gjøre API kall mot AWS Rekognition og lese fra S3.

## A. Kodeendringer og forbedringer

* Fjern hardkodingen av service_name, slik at du kan bruke ditt kandidatnummer eller noe annet som service navn.
* Se etter andre hard-kodede verdier og se om du kan forbedre kodekvaliteten.
* Se på dokumentasjonen til aws_apprunner_service ressursen, og reduser CPU til 256, og Memory til 1024 (defaultverdiene
  er høyere)

## B. Terraform i GitHub Actions

* Utvid din GitHub Actions workflow som lager et Docker image, til også å kjøre terraformkoden
* På hver push til main, skal Terraformkoden kjøres etter jobber som bygger Docker container image
* Du må lege til Terraform provider og backend-konfigurasjon. Dette har Kjell glemt. Du kan bruke samme S3 bucket
  som vi har brukt til det formålet i øvingene.
* Beskriv også hvilke endringer, om noen, sensor må gjøre i sin fork, GitHub Actions workflow eller kode for å få denne til å kjøre i sin fork.

## Assumptions and Running task 3

I have assumed that "access_role_arn" and "image_identifier" (variables arn and img_id) aren't sensitive, as I could not find a consistent consensus online.
If they are I would put them in a .tfvars file and add them as github secrets.

* The program needs the AWS IAM keys that was added as GitHub secrets earlier.
* To get the program to run properly you need to have an existing ECR Repository. You will have to replace the current variable "img_id" in variables.tf with the location of this repository. You can find the location by accessing the repo via AWS Elastic Container Registry
* You will also need to change the ECR_REGISTRY and ECR_REPOSITORY in img-ecr-pipeline.yml where ECR_REGISTRY needs to be (xxx.dkr.ecr.<region>.amazonaws.com), and ECR_REPOSITORY needs to be the name of your ECR repository
* Furthermore you will have to change the variable "arn" in variables.tf to your instance of the AppRunnerECRAccessRole, which you can copy from roles in the IAM part of AWS

Når sensoren evaluerer oppgaven, vil han/hun:

* Sjekke ditt repository og gå til fanen "Actions" på GitHub for å bekrefte at Workflows faktisk fungerer som de skal.
* Vurdere drøftelsesoppgavene. Du må opprette en "Readme" for besvarelsen i ditt repository. Denne "Readme"-filen skal
  inneholde en grundig beskrivelse og drøfting av oppgavene.
* Sensoren vil opprette en "fork" (en kopi) av ditt repository og deretter kjøre GitHub Actions Workflows med sin egen
  AWS- og GitHub-bruker for å bekrefte at alt fungerer som forventet.

# Evaluering

- Oppgave 1. Kjells Pythonkode - 20 Poeng
- Oppgave 2. Overgang til Java og Spring Boot - 15 Poeng
- Oppgave 3. Terraform, AWS Apprunner og IAC - 15 Poeng
- Oppgave 4. Feedback -30 Poeng
- Oppgave 5. Drøfteoppgaver - 20 poeng

# Oppgavebeskrivelse

I et pulserende teknologisamfunn på Grünerløkka, Oslo, har en livlig oppstart ved navn 'VerneVokterne' funnet
sitt eget nisjeområde innenfor helsesektoren. De utvikler banebrytende programvare for bildebehandling som er
designet
for å sikre at helsepersonell alltid bruker personlig verneutstyr (PPE). Med en lidenskap for innovasjon og et sterkt
ønske om å forbedre arbeidssikkerheten, har 'VerneVokterne' samlet et team av dyktige utviklere, engasjerte designere og
visjonære produktledere.

Selskapet hadde tidligere en veldig sentral utvikler som heter Kjell. Kjell hadde en unik tilnærming til kode,
Dessverre var kvaliteten på Kjells kode, for å si det pent, "kreativ."

Som nyansatt har du blitt gitt den utfordrende oppgaven å overta etter "Kjell," som ikke lenger er en del av selskapet.

![Logo](img/logo.png "Assignment logo")


# Oppgave 4. Feedback

## A. Utvid applikasjonen og legg inn "Måleinstrumenter"

I denne oppgaven får dere stor kreativ frihet i å utforske tjenesten Rekognition. Derw skal lage ny og relevant funksjonalitet.
Lag minst et nytt endepunkt, og utvid gjerne også den eksisterende koden med mer funksjonalitet.
Se på dokumentasjonen; https://aws.amazon.com/rekognition/

### Oppgave

* Nå som dere har en litt større kodebase. Gjør nødvendige endringer i Java-applikasjonen til å bruke Micrometer
  rammeverket for Metrics, og konfigurer  for leveranse av Metrics til CloudWatch
* Dere kan detetter selv velge hvordan dere implementerer måleinstrumenter i koden.

Med måleinstrumenter menes i denne sammenhengen ulike typer "meters" i micrometer rammeverket for eksempel;

* Meter
* Gauge
* Timer
* LongTaskTimer
* DistributionSummary

Dere skal skrive en kort begrunnelse for hvorfor dere har valgt måleinstrumentene dere har gjort, og valgene må  være relevante.
Eksempelvis vil en en teller som øker hver gang en metode blir kalt ikke bli vurdert som en god besvarelse, dette fordi denne
metrikkene allerede leveres av Spring Boot/Actuator.

### Vurderingskriterier

* Hensikten med å utvide kodebasen er å få flere naturlige steder å legge inn måleinstrumenter. Det gis ikke poeng for et stort kodevolum, men en god besvarelse vil legge til virkelig og nyttig funksjonalitet.
* En god besvarelse registrer både tekniske, og foretningsmessig metrikker.
* En god besvarelse bør bruke minst tre ulike måleinstrumenter på en god og relevant måte.

### B. CloudWatch Alarm og Terraform moduler

Lag en CloudWatch-alarm som sender et varsel på Epost dersom den utløses.Dere velger selv kriteriet for kriterier til at alarmen
skal løses ut, men dere  må skrive en kort redgjørelse for valget.

Alarmen skal lages ved hjelp av Terraformkode. Koden skal lages som en separat Terraform modul. Legg vekt på å unngå
hardkoding  av verdier i modulen for maksimal gjenbrukbarhet. Pass samtidig på at brukere av modulen ikke må sette mange
variabler når de inkluderer den i koden sin.

# Oppgave 5. Drøfteoppgaver

## Det Første Prinsippet - Flyt

### A. Kontinuerlig Integrering

Forklar hva kontinuerlig integrasjon (CI) er og diskuter dens betydning i utviklingsprosessen. I ditt svar,
vennligst inkluder:

- En definisjon av kontinuerlig integrasjon.
- Fordelene med å bruke CI i et utviklingsprosjekt - hvordan CI kan forbedre kodekvaliteten og effektivisere utviklingsprosessen.
- Hvordan jobber vi med CI i GitHub rent praktisk? For eskempel i et utviklingsteam på fire/fem utivklere?

Continuous Integration, also known as CI, is a software development practice aiming to improve the quality and efficiency of code by frequently merging code changes into a central repository, after which automated builds and tests are run.
This method of development avoids long periods of development without integration, improves the quality of the code and reduces the overall time it takes to find and address bugs. 
CI relies heavily on version control systems like git to manage and track the changes to the codebase, with a team of developers usually working on branches until the feature they are working on can be considered ready, after which they merge that branch into the main or master branch.
When such a merge is initiated it will be reviewed by a peer to ensure the quality of the code. Should a build or test fail, or a mistake by a developer accidentally take down the application, the development team will be alerted and able to immediatly start working on bringing it back to a "green" state.
By relying on commiting many frequent and smaller code changes, tracing the root causes of issues and reducing difficult to trace bugs becomes easier.

### B. Sammenligning av Scrum/Smidig og DevOps fra et Utviklers Perspektiv

I denne oppgaven skal du som utvikler reflektere over og sammenligne to sentrale metodikker i moderne
programvareutvikling: Scrum/Smidig og DevOps. Målet er å forstå hvordan valg av metodikk kan påvirke kvaliteten og
leveransetempoet i utvikling av programvare.

### Oppgave

1. **Scrum/Smidig Metodikk:**

- Beskriv kort, hovedtrekkene i Scrum metodikk og dens tilnærming til programvareutvikling.
- Diskuter eventuelle utfordringer og styrker ved å bruke Scrum/Smidig i programvareutviklingsprosjekter.

Scrum is a software development method that aims to break work into smaller goals to be completed over a certain timeframe commonly called sprints. Each sprint is planned out beforehand, where the goal and backlog of the sprint is planned.
The development team has a daily meeting known as "Daily Scrums" to assess the current progress and observe what needs to be prioritized.
At the end of each sprint cycle, a review is held with stakeholders to liaise with them on feedback expectations and coming plans. The result of the sprint is then demonstrated who is informed about works in progress.
A further meeting is held internally amongst the team to analyze the strengths and weaknesses of the completed sprint, and future areas of improvements, after which the process begins anew.
This method allows for flexibility and continuous feedback, and by bringing in a decision-making authority to the operational level the team can reprioritize tasks based on customer requirements.
However this method of development isn't without it's flaws. The time spent conducting meetings such as daily scrum, usually conducted over the span of a maximum of 15 minutes, can often surpass their timeboxing, which takes away time that could be used for more productive work like further developing the product.
The Scrum model of work may also pose problems for a number of different teams, like teams that are geographically distant from eachother, work part time or have highly specialized members.

2. **DevOps Metodikk:**

- Forklar grunnleggende prinsipper og praksiser i DevOps, spesielt med tanke på integrasjonen av utvikling og drift.
- Analyser hvordan DevOps kan påvirke kvaliteten og leveransetempoet i programvareutvikling.
- Reflekter over styrker og utfordringer knyttet til bruk av DevOps i utviklingsprosjekter.

The DevOps work model integrates the development team and the operations team into a single team of engineers work across the entire application lifcycle.
One of the fundamental practices of DevOps is the constant flow of frequent, but small updates in the form of Continuous Integration. Through Continuous Delivery these changes are automatically built, tested and prepared for release. They are then deployed to a environment for testing and/or deployment.
CI/CD is some of the most important principles within DevOps, but is not all of it. DevOps also contains the concept of Infrastructure as Code, which is a practice where infrastructure is prepared and managed with code and version control, through cloud API-models.
Another aspect of DevOps is the capture, monitoring and analysis of data and logs gathered from applications and infrastructure. This focus on monitoring and logging allows for insights into how users perceives and is impacted by changes and updates.
 The DevOps work model may increase code quality due to the frequent updates allowing for easier tracing of potential bugs, and also increase the delivery pace of the development team since there is an emphasis on smaller commits, which will allow the changes to be deployed more frequently.
DevOps overall is a robust and quality focused work model which allows for a continuous stream of changes, able to build a quality product from the ground up. However, it is difficult to transition an existing project to the DevOps model, which is its greatest difficulty.

3. **Sammenligning og Kontrast:**

- Sammenlign Scrum/Smidig og DevOps i forhold til deres påvirkning på programvarekvalitet og leveransetempo.
- Diskuter hvilke aspekter ved hver metodikk som kan være mer fordelaktige i bestemte utviklingssituasjoner.

Agile methodologies such as Scrum prioritize iterative development cycles with an emphasis on regular feedback which allows for early detection and correction of potential issues, improving alignment with the customer's vison.
Through close collaboration with stakeholders and customers, the understanding of the customer requirements will increase, and help meeting said requirements, which in turn will increase customer satisfaction.
This close collaboration with stakeholders and the iterative nature of Agile allows it to rapidly adapt to changing requirements during the development process.
It is however this reliance of feedback that is one of its greatest drawbacks, as it creates an dependency on the continuous feedback which can lead to delays in addressing issues if not recieved in a timely manner.
It may also be vulnerable to scope creep in the case of constantly changing requirements going out of control, which can negatively impact the delivery timelines and lead to delays.

DevOps practices, on the other hand has a large emphasis on automation and Continuous Integration, which can assist in remedying integration issues earlier in the development process. 
Adding to this, the focus on automated testing ensures a consistent evaluation of code quality, which lessens the chance of introducing bugs into the code.
The focus on automation and collaboration allows it to facilitate rapid and frequent releases, with the automation of repetitive tasks reducing the chanses of human error and accelerating the overall delivery process.
Implementing DevOps practices may however prove challenging, especially for existing projects, as it requires significant groundwork for its implementation. This may require significant changes to existing processes and may lead challenges for the team.
The teams may face a learning curve while adopting the new tools and practices necessary for the successful implementation of DevOps, which lead to inital slowdowns in productivity until everyone is brought up to speed. 

Overall they both have their strengths and weaknesses, with DevOps focusing on faster and continous integration, delivery and development, while Agile focuses on iterative development and feedback to meet customer expectations.
Which one to choose depends heavily on the project and which stage of development it is in.
I would not bring DevOps methodology into an already existing project that wasn't employing it, but rather go for Agile. This is because Agile doesn't require much in terms of a technical aspect, but relies more on development structure and work ethics, as opposed to DevOps which has a steep technical requirement which would cause a large slowdown in productivity.
In the event that I was partaking in a new project, I would likely go for the DevOps approach as it allows the enforcement of higher quality code from the get-go, provided that the tests also were of sufficient quality.
This is not to discount the quality of Agile, as I believe a combination of the two has great potential, with the DevOps aspect ensuring quality and regular deliveries, while tight cooperation with the customer/stakeholders will keep the project in line with their vision.

### C. Det Andre Prinsippet - Feedback

Tenk deg at du har implementert en ny funksjonalitet i en applikasjon du jobber med. Beskriv hvordan du vil
etablere og bruke teknikker vi har lært fra "feedback" for å sikre at den nye funksjonaliteten møter brukernes behov.
Behovene Drøft hvordan feedback bidrar til kontinuerlig forbedring og hvordan de kan integreres i ulike stadier av
utviklingslivssyklusen.

## LYKKE TIL OG HA DET GØY MED OPPGAVEN!