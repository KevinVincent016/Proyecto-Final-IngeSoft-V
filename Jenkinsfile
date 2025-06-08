pipeline {
    agent any

    tools {
        maven 'MVN'
        jdk 'JDK 11'
    }

    environment {
        DOCKERHUB_USER = 'sanjodb'
        DOCKER_CREDENTIALS_ID = 'docker_password'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service locust'
        K8S_NAMESPACE = 'ecommerce'
        KUBECONFIG = 'C:\\Users\\Santi\\.kube\\config'
    }

    parameters {
        booleanParam(
            name: 'GENERATE_RELEASE_NOTES',
            defaultValue: true,
            description: 'Generate automatic release notes'
        )
        string(
            name: 'BUILD_TAG',
            defaultValue: "${env.BUILD_ID}",
            description: 'Tag for release notes identification'
        )
    }

   stages {

           stage('Init') {
               steps {
                   script {
                       def profileConfig = [
                           master : ['prod', '-prod'],
                           stage: ['stage', '-stage']
                       ]
                       def config = profileConfig.get(env.BRANCH_NAME, ['dev', '-dev'])

                       env.SPRING_PROFILES_ACTIVE = config[0]
                       env.IMAGE_TAG = config[0]
                       env.DEPLOYMENT_SUFFIX = config[1]

                       echo "📦 Branch: ${env.BRANCH_NAME}"
                       echo "🌱 Spring profile: ${env.SPRING_PROFILES_ACTIVE}"
                       echo "🏷️ Image tag: ${env.IMAGE_TAG}"
                       echo "📂 Deployment suffix: ${env.DEPLOYMENT_SUFFIX}"

                   }
               }
           }

        stage('Ensure Namespace') {
            steps {
                script {
                    def ns = env.K8S_NAMESPACE
                    bat "kubectl get namespace ${ns} || kubectl create namespace ${ns}"
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/SanjoDB/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Verify Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker --version'
                bat 'kubectl config current-context'
            }
        }

        stage('Build & Package') {
            when { anyOf { branch 'master'; branch 'stage'; branch 'dev' } }
            steps {
                bat "mvn clean package -DskipTests"
            }
        }

/*

        stage('Run SonarQube Analysis') {
            when { branch 'master' }
            tools {
                jdk 'JDK 17'
            }
            environment {
                JAVA_HOME = tool 'JDK 17'
                PATH = "${JAVA_HOME}/bin:${env.PATH}"
                scannerHome = tool 'lil-sonar-tool'
            }
            steps {
                script {
                    def javaServices = [
                        'api-gateway',
                        'cloud-config',
                        'favourite-service',
                        'order-service',
                        'payment-service',
                        'product-service',
                        'proxy-client',
                        'service-discovery',
                        'shipping-service',
                        'user-service',
                        'e2e-tests'
                    ]

                    withSonarQubeEnv(credentialsId: 'sonarqube_password', installationName: 'lil sonar installation') {
                        javaServices.each { service ->
                            dir(service) {
                                bat "${scannerHome}/bin/sonar-scanner " +
                                "-Dsonar.projectKey=${service} " +
                                "-Dsonar.projectName=${service} " +
                                '-Dsonar.sources=src ' +
                                '-Dsonar.java.binaries=target/classes'
                            }
                        }

                        dir('locust') {
                            bat "${scannerHome}/bin/sonar-scanner " +
                            '-Dsonar.projectKey=locust ' +
                            '-Dsonar.projectName=locust ' +
                            '-Dsonar.sources=test'
                        }
                    }
                }
            }
        }

*/

        stage('Trivy Vulnerability Scan & Report') {
            when { branch 'stage' }
            environment {
                TRIVY_PATH = 'C:/ProgramData/chocolatey/bin'
            }
            steps {
                script {
                    env.PATH = "${TRIVY_PATH};${env.PATH}"

                    def services = [
                        'api-gateway',
                        'cloud-config',
                        'favourite-service',
                        'order-service',
                        'payment-service',
                        'product-service',
                        'proxy-client',
                        'service-discovery',
                        'shipping-service',
                        'user-service'
                    ]

                    bat """
                    if not exist trivy-reports (
                        mkdir trivy-reports
                    )
                    """

                    services.each { service ->
                        def reportPath = "trivy-reports\\${service}.html"

                        echo "🔍 Escaneando imagen ${IMAGE_TAG} con Trivy para ${service}..."
                        bat """
                        trivy image --format template ^
                            --template "@C:/ProgramData/chocolatey/lib/trivy/tools/contrib/html.tpl" ^
                            --severity HIGH,CRITICAL ^
                            -o ${reportPath} ^
                            ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}
                        """
                    }

                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'trivy-reports',
                        reportFiles: '*.html',
                        reportName: 'Trivy Scan Report'
                    ])
                }
            }
        }

/*

        stage('Build & Push Docker Images') {
            when { anyOf { branch 'stage'; branch 'master' } }
            steps {
                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'docker_password')]) {
                    bat "docker login -u ${DOCKERHUB_USER} -p ${docker_password}"

                    script {
                        SERVICES.split().each { service ->
                            bat "docker build -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} .\\${service}"
                            bat "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

*/

         stage('Unit Tests') {
                    when {
                        anyOf {
                            branch 'dev'; branch 'stage'
                            expression { env.BRANCH_NAME.startsWith('feature/') }
                        }
                    }
                    steps {
                        script {
                            ['user-service', 'product-service'].each {
                                bat "mvn test -pl ${it}"
                            }
                        }
                        junit '**/target/surefire-reports/*.xml'
                    }
                }


        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'dev'; branch 'stage';
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                    allOf { not { branch 'master' }; not { branch 'release' } }
                }
            }
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        bat "mvn verify -pl ${it}"
                    }
                }
                junit '**/target/failsafe-reports/TEST-*.xml'
            }
        }

/*

        stage('E2E Tests') {
            when {
                anyOf {
                    branch 'stage';
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
            steps {
                bat "mvn verify -pl e2e-tests"
                junit 'e2e-tests/target/failsafe-reports/*.xml'
            }
        }

    stage('Start containers for testing') {
              when {
                     anyOf {
                         branch 'stage'
                         expression { env.BRANCH_NAME.startsWith('feature/') }
                     }
                 }
         steps {
             script {
                 powershell '''
                  # Function to wait for a service to be healthy
                 function Wait-ForHealthCheck {
                     param(
                         [string]$Url,
                         [string]$ServiceName,
                         [int]$TimeoutSeconds = 300
                     )

                     Write-Host "⌛ Waiting for $ServiceName..." -ForegroundColor Yellow
                     $startTime = Get-Date

                     do {
                         try {
                             $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
                             if ($response.status -eq "UP") {
                                  Write-Host "✅ $ServiceName is healthy!" -ForegroundColor Green
                                 return $true
                             }
                         }
                         catch {
                                        # Retry until successful
                         }

                         Start-Sleep -Seconds 5
                         $elapsed = (Get-Date) - $startTime

                         if ($elapsed.TotalSeconds -gt $TimeoutSeconds) {
                             Write-Host "❌ Timeout waiting for $ServiceName" -ForegroundColor Red
                             return $false
                         }

                           Write-Host "⌛ Waiting for $ServiceName... ($([int]$elapsed.TotalSeconds)s)" -ForegroundColor Yellow
                     } while ($true)
                 }

                  # Function to wait for health check with complex JSON
                 function Wait-ForHealthCheckWithJq {
                     param(
                         [string]$Url,
                         [string]$ServiceName,
                         [int]$TimeoutSeconds = 300
                     )

                     Write-Host "⌛ Waiting for $ServiceName..." -ForegroundColor Yellow
                     $startTime = Get-Date

                     do {
                         try {
                             $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
                             if ($response.status -eq "UP") {
                                  Write-Host "✅ $ServiceName is healthy!" -ForegroundColor Green
                                 return $true
                             }
                         }
                         catch {
                                 # Retry until successful
                         }

                         Start-Sleep -Seconds 5
                         $elapsed = (Get-Date) - $startTime

                         if ($elapsed.TotalSeconds -gt $TimeoutSeconds) {
                              Write-Host "❌ Timeout waiting for $ServiceName" -ForegroundColor Red
                             return $false
                         }

                          Write-Host "⌛ Waiting for $ServiceName... ($([int]$elapsed.TotalSeconds)s)" -ForegroundColor Yellow
                     } while ($true)
                 }

                 try {
                       # create Docker network if it doesn't exist
                     Write-Host "🌐 Creating Docker network..." -ForegroundColor Cyan
                     docker network create ecommerce-test 2>$null
                     if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne 1) {
                         throw "Error creating Docker network"
                     }

                     # 1. ZIPKIN
                      Write-Host "🚀 Starting ZIPKIN..." -ForegroundColor Cyan
                     docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin
                      if ($LASTEXITCODE -ne 0) { throw "Error starting Zipkin" }

                     # 2. EUREKA (Service Discovery)
                      Write-Host "🚀 Starting EUREKA..." -ForegroundColor Cyan
                     docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         sanjodb/service-discovery:${env:IMAGE_TAG}
                       if ($LASTEXITCODE -ne 0) { throw "Error starting Eureka" }


                     if (!(Wait-ForHealthCheck -Url "http://localhost:8761/actuator/health" -ServiceName "EUREKA")) {
                         throw "Eureka could not be started correctly"
                     }

                     # 3. CLOUD-CONFIG
                     Write-Host "🚀 Starting CLOUD-CONFIG..." -ForegroundColor Cyan
                     docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ `
                         -e EUREKA_INSTANCE=cloud-config-container `
                         sanjodb/cloud-config:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Cloud Config" }

                     if (!(Wait-ForHealthCheck -Url "http://localhost:9296/actuator/health" -ServiceName "CLOUD-CONFIG")) {
                         throw "CLOUD-CONFIG could not be started correctly"
                     }

                     # 4. ORDER-SERVICE
                     Write-Host "🚀 Starting ORDER-SERVICE..." -ForegroundColor Cyan
                     docker run -d --name order-service-container --network ecommerce-test -p 8300:8300 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=order-service-container `
                         sanjodb/order-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Order Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8300/order-service/actuator/health" -ServiceName "ORDER-SERVICE")) {
                         throw "ORDER-SERVICE could not be started correctly"
                     }

                     # 5. PAYMENT-SERVICE
                     Write-Host "🚀 Starting PAYMENT..." -ForegroundColor Cyan
                     docker run -d --name payment-service-container --network ecommerce-test -p 8400:8400 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=payment-service-container `
                         sanjodb/payment-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Payment Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8400/payment-service/actuator/health" -ServiceName "PAYMENT-SERVICE")) {
                         throw "PAYMENT-SERVICE could not be started correctly"
                     }

                     # 6. PRODUCT-SERVICE
                     Write-Host "🚀 Starting PRODUCT..." -ForegroundColor Cyan
                     docker run -d --name product-service-container --network ecommerce-test -p 8500:8500 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=product-service-container `
                         sanjodb/product-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Product Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8500/product-service/actuator/health" -ServiceName "PRODUCT-SERVICE")) {
                         throw "PRODUCT-SERVICE could not be started correctly"
                     }

                     # 7. SHIPPING-SERVICE
                     Write-Host "🚀 Starting SHIPPING..." -ForegroundColor Cyan
                     docker run -d --name shipping-service-container --network ecommerce-test -p 8600:8600 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=shipping-service-container `
                         sanjodb/shipping-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Shipping Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8600/shipping-service/actuator/health" -ServiceName "SHIPPING-SERVICE")) {
                         throw "SHIPPING-SERVICE could not be started correctly"
                     }

                     # 8. USER-SERVICE
                     Write-Host "🚀 Starting USER..." -ForegroundColor Cyan
                     docker run -d --name user-service-container --network ecommerce-test -p 8700:8700 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=user-service-container `
                         sanjodb/user-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting User Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8700/user-service/actuator/health" -ServiceName "USER-SERVICE")) {
                         throw "USER-SERVICE could not be started correctly"
                     }

                     # 9. FAVOURITE-SERVICE
                     Write-Host "🚀 Starting FAVOURITE..." -ForegroundColor Cyan
                     docker run -d --name favourite-service-container --network ecommerce-test -p 8800:8800 `
                         -e SPRING_PROFILES_ACTIVE=dev `
                         -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 `
                         -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 `
                         -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka `
                         -e EUREKA_INSTANCE=favourite-service-container `
                         sanjodb/favourite-service:${env:IMAGE_TAG}
                     if ($LASTEXITCODE -ne 0) { throw "Error starting Favourite Service" }

                     if (!(Wait-ForHealthCheckWithJq -Url "http://localhost:8800/favourite-service/actuator/health" -ServiceName "FAVOURITE-SERVICE")) {
                         throw "FAVOURITE-SERVICE could not be started correctly"
                     }

                     Write-Host "✅ All containers are up and healthy." -ForegroundColor Green
                 }
                 catch {
                     Write-Host "❌ Error: $_" -ForegroundColor Red
                     Write-Host "🧹 Cleaning up containers..." -ForegroundColor Yellow

                     # Cleanup in case of error
                     $containers = @(
                         "favourite-service-container",
                         "user-service-container",
                         "shipping-service-container",
                         "product-service-container",
                         "payment-service-container",
                         "order-service-container",
                         "cloud-config-container",
                         "service-discovery-container",
                         "zipkin-container"
                        )

                     foreach ($container in $containers) {
                         docker stop $container 2>$null
                         docker rm $container 2>$null
                        }

                     docker network rm ecommerce-test 2>$null
                     throw "Failed to start containers"
                    }
                 '''
                }
            }
        }





       stage('Run Load Tests with Locust') {

           when {
                anyOf {
                    branch 'stage'
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
           steps {
               script {
                   bat '''
                   echo 🚀 Starting Locust for order-service...

                   if not exist locust-reports (
                        mkdir locust-reports
                    )

                   docker run --rm --network ecommerce-test ^
                     -v "%CD%/locust-reports:/mnt/locust" ^
                     -v "%CD%\\locust:/mnt" ^
                     -v "%CD%\\locust-results:/app" ^
                     sanjodb/locust:%IMAGE_TAG% ^
                     -f /mnt/test/order-service/locustfile.py ^
                     --host http://order-service-container:8300 ^
                     --headless -u 5 -r 1 -t 1m ^
                     --only-summary ^
                     --html /mnt/locust/order-service-report.html

                   echo 🚀 Starting Locust for payment-service...

                   docker run --rm --network ecommerce-test ^
                     -v "%CD%/locust-reports:/mnt/locust" ^
                     -v "%CD%\\locust:/mnt" ^
                     -v "%CD%\\locust-results:/app" ^
                     sanjodb/locust:%IMAGE_TAG% ^
                     -f /mnt/test/payment-service/locustfile.py ^
                     --host http://payment-service-container:8400 ^
                     --headless -u 5 -r 1 -t 1m ^
                     --only-summary ^
                     --html /mnt/locust/payment-service-report.html

                   echo 🚀 Starting Locust for favourite-service...

                   docker run --rm --network ecommerce-test ^
                     -v "%CD%/locust-reports:/mnt/locust" ^
                     -v "%CD%\\locust:/mnt" ^
                     -v "%CD%\\locust-results:/app" ^
                     sanjodb/locust:%IMAGE_TAG% ^
                     -f /mnt/test/favourite-service/locustfile.py ^
                     --host http://favourite-service-container:8800 ^
                     --headless -u 5 -r 1 -t 1m ^
                     --only-summary ^
                     --html /mnt/locust/favourite-service-report.html

                   echo ✅ Tests completed
                   '''
               }
           }
       }

       stage('Run Stress Tests with Locust') {
           when {
                anyOf {
                    branch 'stage'
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
           steps {
               script {
                   bat '''
                   echo 🔥 Starting Locust for stress testing...

                   docker run --rm --network ecommerce-test ^
                   -v "%CD%/locust-reports:/mnt/locust" ^
                   -v "%CD%\\locust:/mnt" ^
                   -v "%CD%\\locust-results:/app" ^
                   sanjodb/locust:%IMAGE_TAG% ^
                   -f /mnt/test/order-service/locustfile.py ^
                   --host http://order-service-container:8300 ^
                   --headless -u 10 -r 1 -t 1m ^
                   --only-summary ^
                   --html /mnt/locust/stress-order-service-report.html

                   docker run --rm --network ecommerce-test ^
                   -v "%CD%/locust-reports:/mnt/locust" ^
                   -v "%CD%\\locust:/mnt" ^
                   -v "%CD%\\locust-results:/app" ^
                   sanjodb/locust:%IMAGE_TAG% ^
                   -f /mnt/test/payment-service/locustfile.py ^
                   --host http://payment-service-container:8400 ^
                   --headless -u 10 -r 1 -t 1m ^
                   --only-summary ^
                   --html /mnt/locust/stress-payment-service-report.html

                   docker run --rm --network ecommerce-test ^
                   -v "%CD%/locust-reports:/mnt/locust" ^
                   -v "%CD%\\locust:/mnt" ^
                   -v "%CD%\\locust-results:/app" ^
                   sanjodb/locust:%IMAGE_TAG% ^
                   -f /mnt/test/favourite-service/locustfile.py ^
                   --host http://favourite-service-container:8800 ^
                   --headless -u 10 -r 1 -t 1m ^
                   --only-summary ^
                   --html /mnt/locust/stress-favourite-service-report.html

                   echo ✅ Stress tests completed
                   '''
               }
           }
       }



       stage('Stop and remove containers') {
                when {
                       anyOf {
                           branch 'stage'
                           expression { env.BRANCH_NAME.startsWith('feature/') }
                       }
                }
           steps {
               script {
                   bat """
                   echo 🛑 Stopping and removing containers...

                   docker rm -f locust || exit 0
                   docker rm -f favourite-service-container || exit 0
                   docker rm -f user-service-container || exit 0
                   docker rm -f shipping-service-container || exit 0
                   docker rm -f product-service-container || exit 0
                   docker rm -f payment-service-container || exit 0
                   docker rm -f order-service-container || exit 0
                   docker rm -f cloud-config-container || exit 0
                   docker rm -f service-discovery-container || exit 0
                   docker rm -f zipkin-container || exit 0

                   echo 🧹 All containers removed
                   """
               }
           }
       }

        stage('Deploy Common Config') {
            when { anyOf { branch 'master' } }
            steps {
                bat "kubectl apply -f k8s\\common-config.yaml -n ${K8S_NAMESPACE}"
            }
        }

        stage('Deploy Core Services') {
            when { anyOf { branch 'master' } }
            steps {
                bat "kubectl apply -f k8s\\zipkin -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/zipkin -n ${K8S_NAMESPACE} --timeout=200s"

                bat "kubectl apply -f k8s\\service-discovery -n ${K8S_NAMESPACE}"
                bat "kubectl set image deployment/service-discovery service-discovery=${DOCKERHUB_USER}/service-discovery:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/service-discovery -n ${K8S_NAMESPACE} --timeout=500s"

                bat "kubectl apply -f k8s\\cloud-config -n ${K8S_NAMESPACE}"
                bat "kubectl set image deployment/cloud-config cloud-config=${DOCKERHUB_USER}/cloud-config:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/cloud-config -n ${K8S_NAMESPACE} --timeout=500s"
            }
        }

        stage('Deploy Microservices') {
            when { anyOf { branch 'master'; } }
            steps {
                script {
                    def appServices = ['user-service', 'product-service', 'order-service','favourite-service','payment-service']
                    echo "👻"
                    appServices.each { svc ->
                        bat "kubectl apply -f k8s\\${svc} -n ${K8S_NAMESPACE}"
                        bat "kubectl set image deployment/${svc} ${svc}=${DOCKERHUB_USER}/${svc}:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                        bat "kubectl set env deployment/${svc} SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                        bat "kubectl rollout status deployment/${svc} -n ${K8S_NAMESPACE} --timeout=500s"
                    }
                }
            }
        }

*/

        stage('Generate and Archive Release Notes') {
            when {
                branch 'master'
            }
            steps {
                bat '''
                echo "📝 Generando Release Notes con convco..."
                convco changelog > RELEASE_NOTES.md
                '''
                archiveArtifacts artifacts: 'RELEASE_NOTES.md', fingerprint: true
            }
        }

    }

    post {
        success {
            script {
                echo "✅ Pipeline completed successfully for ${env.BRANCH_NAME} branch."
                echo "📊 Environment: ${env.SPRING_PROFILE}"

                if (env.BRANCH_NAME == 'master') {
                    echo "🚀 Production deployment completed successfully!"
                } else if (env.BRANCH_NAME == 'stage') {
                    echo "🎯 Staging deployment completed successfully!"
                    publishHTML([
                        reportDir: 'locust-reports',
                        reportFiles: 'order-service-report.html, payment-service-report.html, favourite-service-report.html',
                        reportName: 'Locust Stress Test Reports',
                        keepAll: true
                    ])
                } else {
                    echo "🔧 Development tests completed successfully!"
                }
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed for ${env.BRANCH_NAME} branch."
                echo "🔍 Check the logs for details."
                echo "📧 Notify the development team about the failure."

            }
        }
        unstable {
            script {
                echo "⚠️ Pipeline completed with warnings for ${env.BRANCH_NAME} branch."
                echo "🔍 Some tests may have failed. Review test reports."
            }
        }
        
    }
}