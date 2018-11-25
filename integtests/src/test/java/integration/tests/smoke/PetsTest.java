/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package integration.tests.smoke;

import dom.owners.Owner;
import dom.owners.Owners;
import dom.pets.Pet;
import dom.pets.PetSpecies;
import dom.pets.Pets;
import fixture.owners.scenario.OwnersFixture;
import fixture.pets.PetClinicAppTearDownFixture;
import fixture.pets.scenario.PetsFixture;
import integration.tests.PetClinicAppIntegTest;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import javax.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;

import static integration.tests.util.Util.causalChainContains;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PetsTest extends PetClinicAppIntegTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    Pets pets;
    @Inject
    Owners owners;

    FixtureScript fixtureScript;

    public static class ListAll extends PetsTest {

        @Test
        public void happyCase() throws Exception {

            // given
            fixtureScript = new PetsFixture();
            fixtureScripts.runFixtureScript(fixtureScript, null);
            nextTransaction();

            // when
            final List<Pet> all = wrap(pets).listAll();

            // then
            assertThat(all.size(), is(3));

            Pet pet = wrap(all.get(0));
            assertThat(pet.getName(), is("Skye"));
        }

        @Test
        public void whenNone() throws Exception {

            // given
            fixtureScript = new PetClinicAppTearDownFixture();
            fixtureScripts.runFixtureScript(fixtureScript, null);
            nextTransaction();

            // when
            final List<Pet> all = wrap(pets).listAll();

            // then
            assertThat(all.size(), is(0));
        }
    }

    public static class Create extends PetsTest {

        @Test
        public void happyCase() throws Exception {

            // given
            fixtureScript = new OwnersFixture();
            fixtureScripts.runFixtureScript(fixtureScript, null);
            nextTransaction();

            final Owner owner = fixtureScript.lookup("owners-fixture/owner-for-bill/item-1", Owner.class);
            assertThat(owner, is(notNullValue()));

            // when
            wrap(pets).create("Bonzo", PetSpecies.Dog, owner);

            // then
            final List<Pet> all = wrap(pets).listAll();
            assertThat(all.size(), is(1));
            final Pet pet = all.get(0);

            assertThat(pet.getName(), is("Bonzo"));
            assertThat(pet.getSpecies(), is(PetSpecies.Dog));
            assertThat(pet.getOwner(), is(owner));
        }

        @Test
        public void whenAlreadyExists() throws Exception {

            // given
            fixtureScript = new OwnersFixture();
            fixtureScripts.runFixtureScript(fixtureScript, null);
            nextTransaction();

            final Owner owner = fixtureScript.lookup("owners-fixture/owner-for-bill/item-1", Owner.class);
            assertThat(owner, is(notNullValue()));

            wrap(pets).create("Bonzo", PetSpecies.Dog, owner);
            nextTransaction();

            // then
            expectedException.expectCause(causalChainContains(SQLIntegrityConstraintViolationException.class));

            // when
            wrap(pets).create("Bonzo", PetSpecies.Dog, owner);
            nextTransaction();
        }

    }

}