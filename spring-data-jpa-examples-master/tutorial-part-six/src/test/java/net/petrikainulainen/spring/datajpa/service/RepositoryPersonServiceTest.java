package net.petrikainulainen.spring.datajpa.service;

import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.expr.BooleanExpression;
import net.petrikainulainen.spring.datajpa.dto.PersonDTO;
import net.petrikainulainen.spring.datajpa.model.Person;
import net.petrikainulainen.spring.datajpa.model.QPerson;
import net.petrikainulainen.spring.datajpa.model.PersonTestUtil;
import net.petrikainulainen.spring.datajpa.repository.PersonRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author vominhtung
 */
public class RepositoryPersonServiceTest {

    private static final Long PERSON_ID = Long.valueOf(5);
    private static final String FIRST_NAME = "Foo";
    private static final String FIRST_NAME_UPDATED = "FooUpdated";
    private static final String LAST_NAME = "Bar";
    private static final String LAST_NAME_UPDATED = "BarUpdated";
    private static final String SEARCH_TERM = "foo";
    
    private RepositoryPersonService personService;

    private PersonRepository personRepositoryMock;

    @Before
    public void setUp() {
        personService = new RepositoryPersonService();

        personRepositoryMock = mock(PersonRepository.class);
        personService.setPersonRepository(personRepositoryMock);
    }
    
    @Test
    public void create() {
        PersonDTO created = PersonTestUtil.createDTO(null, FIRST_NAME, LAST_NAME);
        Person persisted = PersonTestUtil.createModelObject(PERSON_ID, FIRST_NAME, LAST_NAME);
        
        when(personRepositoryMock.save(any(Person.class))).thenReturn(persisted);
        
        Person returned = personService.create(created);

        ArgumentCaptor<Person> personArgument = ArgumentCaptor.forClass(Person.class);
        verify(personRepositoryMock, times(1)).save(personArgument.capture());
        verifyNoMoreInteractions(personRepositoryMock);

        assertPerson(created, personArgument.getValue());
        assertEquals(persisted, returned);
    }
    
    @Test
    public void delete() throws PersonNotFoundException {
        Person deleted = PersonTestUtil.createModelObject(PERSON_ID, FIRST_NAME, LAST_NAME);
        when(personRepositoryMock.findOne(PERSON_ID)).thenReturn(deleted);
        
        Person returned = personService.delete(PERSON_ID);
        
        verify(personRepositoryMock, times(1)).findOne(PERSON_ID);
        verify(personRepositoryMock, times(1)).delete(deleted);
        verifyNoMoreInteractions(personRepositoryMock);
        
        assertEquals(deleted, returned);
    }
    
    @Test(expected = PersonNotFoundException.class)
    public void deleteWhenPersonIsNotFound() throws PersonNotFoundException {
        when(personRepositoryMock.findOne(PERSON_ID)).thenReturn(null);
        
        personService.delete(PERSON_ID);
        
        verify(personRepositoryMock, times(1)).findOne(PERSON_ID);
        verifyNoMoreInteractions(personRepositoryMock);
    }
    
    @Test
    public void findAll() {
        List<Person> persons = new ArrayList<Person>();
        when(personRepositoryMock.findAll(any(Sort.class))).thenReturn(persons);
        
        List<Person> returned = personService.findAll();

        ArgumentCaptor<Sort> sortArgument = ArgumentCaptor.forClass(Sort.class);
        verify(personRepositoryMock, times(1)).findAll(sortArgument.capture());

        verifyNoMoreInteractions(personRepositoryMock);

        Sort actualSort = sortArgument.getValue();
        assertEquals(Sort.Direction.ASC, actualSort.getOrderFor("lastName").getDirection());

        assertEquals(persons, returned);
    }
    
    @Test
    public void findById() {
        Person person = PersonTestUtil.createModelObject(PERSON_ID, FIRST_NAME, LAST_NAME);
        when(personRepositoryMock.findOne(PERSON_ID)).thenReturn(person);
        
        Person returned = personService.findById(PERSON_ID);
        
        verify(personRepositoryMock, times(1)).findOne(PERSON_ID);
        verifyNoMoreInteractions(personRepositoryMock);
        
        assertEquals(person, returned);
    }
    
    @Test
    public void search() {
        List<Person> expected = new ArrayList<Person>();
        when(personRepositoryMock.findAll(any(BooleanExpression.class), any(OrderSpecifier.class))).thenReturn(expected);
        
        List<Person> actual = personService.search(SEARCH_TERM);

        ArgumentCaptor<OrderSpecifier> orderArgument = ArgumentCaptor.forClass(OrderSpecifier.class);
        verify(personRepositoryMock, times(1)).findAll(any(BooleanExpression.class), orderArgument.capture());

        verifyNoMoreInteractions(personRepositoryMock);

        OrderSpecifier actualOrder = orderArgument.getValue();
        assertEquals(Order.ASC, actualOrder.getOrder());
        assertEquals(QPerson.person.lastName, actualOrder.getTarget());

        assertEquals(expected, actual);
    }
    
    @Test
    public void update() throws PersonNotFoundException {
        PersonDTO updated = PersonTestUtil.createDTO(PERSON_ID, FIRST_NAME_UPDATED, LAST_NAME_UPDATED);
        Person person = PersonTestUtil.createModelObject(PERSON_ID, FIRST_NAME, LAST_NAME);
        
        when(personRepositoryMock.findOne(updated.getId())).thenReturn(person);
        
        Person returned = personService.update(updated);
        
        verify(personRepositoryMock, times(1)).findOne(updated.getId());
        verifyNoMoreInteractions(personRepositoryMock);
        
        assertPerson(updated, returned);
    }
    
    @Test(expected = PersonNotFoundException.class)
    public void updateWhenPersonIsNotFound() throws PersonNotFoundException {
        PersonDTO updated = PersonTestUtil.createDTO(PERSON_ID, FIRST_NAME_UPDATED, LAST_NAME_UPDATED);
        
        when(personRepositoryMock.findOne(updated.getId())).thenReturn(null);

        personService.update(updated);

        verify(personRepositoryMock, times(1)).findOne(updated.getId());
        verifyNoMoreInteractions(personRepositoryMock);
    }

    private void assertPerson(PersonDTO expected, Person actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), expected.getLastName());
    }

}
