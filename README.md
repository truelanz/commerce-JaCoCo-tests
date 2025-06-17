## Mock e IT tests com jaCoCo - [Projeto base](https://github.com/truelanz/truelanz-commerce)

- O JaCoCo implementa 3 métricas principais para cobertura, sendo:
	- Line Coverage/Statement;
	- Branch Coverage;
	- Cyclomatic complexity: A partir de uma combinação linear apresenta o números de caminhos que necessitam cobertura;

---
- Diamante vermelho: Indica que nenhum teste está cobrindo o branch;

- Diamante amarelo: Indica que o código está parcialmente coberto;
  
- Diamante verde: Indica que todo o branch foi testado e coberto;

- [Plugin maven Jacoco](https://www.eclemma.org/jacoco/trunk/doc/maven.html)
- [Plugin maven Jacoco com configs](https://gist.github.com/oliveiralex/fd320a363a4294860b43c8e9bf63ebfc)
```xml
<plugin>
	<groupId>org.jacoco</groupId>
	<artifactId>jacoco-maven-plugin</artifactId>
	<version>0.8.7</version>
	<configuration>
		<excludes>
		<exclude>com/devsuperior/dscommerce/DscommerceApplication.class</exclude>
			<exclude>com/devsuperior/dscommerce/config/**</exclude>
			<exclude>com/devsuperior/dscommerce/entities/**</exclude>
			<exclude>com/devsuperior/dscommerce/dto/**</exclude>
			<exclude>com/devsuperior/dscommerce/controllers/handlers/**</exclude>
			<exclude>com/devsuperior/dscommerce/services/exceptions/**</exclude>
		</excludes>
	</configuration>
		<executions>
			<execution>
				<goals>
					<goal>prepare-agent</goal>
				</goals>
			</execution>
			<execution>
				<id>jacoco-report</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>report</goal>
				</goals>
				<configuration>
					<outputDirectory>target/jacoco-report</outputDirectory>
				</configuration>
			</execution>
		</executions>
</plugin>
```


`<excludes>` Exclui as classes que não é necessário testes para que não aparece no relatório do JaCoCo. 

Após dar um `mvn install` serão gerados os relatórios da cobertura de teste em `target/jacoco-report/index.html`.
