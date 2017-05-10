/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.component.web;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.builder.ColumnViewElementBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.web.TextWebComponentResources;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Expects the entity to be the {@link com.foreach.across.modules.webcms.domain.WebCmsObject} that owns the components.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope("prototype")
@RequiredArgsConstructor
public class OrderedWebCmsComponentsFormProcessor extends EntityViewProcessorAdapter
{
	private final BootstrapUiFactory bootstrapUiFactory;
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsComponentModelAdminRenderService componentModelAdminRenderService;

	private String[] componentNames = new String[0];
	private boolean renderMembersIfContainer = true;

	/**
	 * Set the names of the component models that should be editable.
	 */
	public void setComponentNames( String... componentNames ) {
		this.componentNames = componentNames;
	}

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		WebCmsObject owner = command.getEntity( WebCmsObject.class );

		String componentObjectId = entityViewRequest.getWebRequest().getParameter( "componentObjectId" );

		command.addExtension(
				"webCmsComponents",
				new ModelsHolder(
						Stream.of( componentNames )
						      .map( componentName -> componentModelService.getComponentModel( componentName, owner ) )
						      .flatMap(
								      model -> {
									      if ( model instanceof ContainerWebCmsComponentModel && renderMembersIfContainer ) {
										      return ( (ContainerWebCmsComponentModel) model ).getMembers().stream();
									      }
									      return Stream.of( model );
								      }
						      )
						      .collect( Collectors.toList() )
						      .toArray( new WebCmsComponentModel[0] )
				)
		);

		if ( componentObjectId != null ) {
			command.addExtension(
					"configuredWebCmsComponents",
					command.getExtension( "webCmsComponents" )
			);
			command.addExtension(
					"webCmsComponents",
					new ModelsHolder( new WebCmsComponentModel[] { componentModelService.getComponentModel( componentObjectId ) } )
			);
			command.addExtension( "singleComponent", Boolean.TRUE );
		}
	}

	@Override
	protected void doControl( EntityViewRequest entityViewRequest,
	                          EntityView entityView,
	                          EntityViewCommand command,
	                          BindingResult bindingResult,
	                          HttpMethod httpMethod ) {
		UrlPathHelper pathHelper = new UrlPathHelper();
		entityView.addAttribute(
				"currentComponentUrl",
				pathHelper.getPathWithinApplication( entityViewRequest.getWebRequest().getNativeRequest( HttpServletRequest.class ) )
		);
		entityView.addAttribute(
				"componentLinkBuilder",
				new InnerComponentLinkBuilder(
						entityViewRequest.getEntityViewContext().getLinkBuilder().update( entityViewRequest.getEntityViewContext().getEntity() )
				)
		);
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			ModelsHolder componentModels = command.getExtension( "webCmsComponents", ModelsHolder.class );
			Arrays.stream( componentModels.models ).forEach( componentModelService::save );

			// redirect to self
			entityView.setRedirectUrl( ServletUriComponentsBuilder.fromCurrentRequest().toUriString() );
		}
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		EntityViewCommand command = entityViewRequest.getCommand();
		ModelsHolder componentModels = command.getExtension( "webCmsComponents", ModelsHolder.class );

		ColumnViewElementBuilder columnViewElementBuilder = builderMap.get( SingleEntityFormViewProcessor.LEFT_COLUMN, ColumnViewElementBuilder.class );
		val componentLinkBuilder = entityView.getAttribute( "componentLinkBuilder", EntityLinkBuilder.class );

		if ( isSingleComponent( command ) ) {
			val componentModel = componentModels.models[0];
			val ownerTrail = bootstrapUiFactory.node( "ul" ).css( "breadcrumb", "wcm-component-owner-trail" );
			val baseUrl = entityViewRequest.getEntityViewContext().getLinkBuilder().update( entityViewRequest.getEntityViewContext().getEntity() );
			WebCmsObject root = entityViewRequest.getEntityViewContext().getEntity( WebCmsObject.class );
			ModelsHolder rootComponents = command.getExtension( "configuredWebCmsComponents", ModelsHolder.class );
			if ( addToOwnerTrail( ownerTrail, componentModel.getObjectId(), componentLinkBuilder, rootComponents, root, baseUrl, false ) ) {
				columnViewElementBuilder.add( ownerTrail );
			}
		}

		for ( int i = 0; i < componentModels.models.length; i++ ) {
			WebCmsComponentModelFormElementBuilder formElement
					= componentModelAdminRenderService.createFormElement( componentModels.models[i], "extensions[webCmsComponents].models[" + i + "]" );
			formElement.showSettings( false );

			columnViewElementBuilder.add( formElement );
		}
	}

	private boolean addToOwnerTrail( NodeViewElementBuilder breadcrumb,
	                                 String objectId,
	                                 EntityLinkBuilder linkBuilder,
	                                 ModelsHolder rootComponents,
	                                 WebCmsObject root,
	                                 String baseUrl,
	                                 boolean createLink ) {
		WebCmsComponent owner = componentRepository.findOneByObjectId( objectId );
		if ( owner != null ) {
			val linkToRoot = ( owner.hasOwner() && owner.getOwnerObjectId().equals( root.getObjectId() ) ) || rootComponents.contains( owner );
			String title = StringUtils.defaultIfBlank( owner.getTitle(), StringUtils.defaultIfBlank( owner.getName(), owner.getComponentType().getName() ) );
			breadcrumb.addFirst(
					bootstrapUiFactory.node( "li" )
					                  .attribute( "title", owner.getName() )
					                  .add(
							                  createLink
									                  ? bootstrapUiFactory.link()
									                                      .url(
											                                      linkToRoot
													                                      ? baseUrl
													                                      : UriComponentsBuilder.fromUriString( linkBuilder.update( owner ) )
													                                                            .queryParam( "from", baseUrl )
													                                                            .toUriString()
									                                      )
									                                      .text( title )
									                  : bootstrapUiFactory.text( title )
					                  )
			);

			if ( owner.hasOwner() && !linkToRoot ) {
				return addToOwnerTrail( breadcrumb, owner.getOwnerObjectId(), linkBuilder, rootComponents, root, baseUrl, true ) || createLink;
			}

			return createLink;
		}

		return false;
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		if ( isSingleComponent( entityViewRequest.getCommand() ) ) {
			container.find( SingleEntityFormViewProcessor.RIGHT_COLUMN, ContainerViewElement.class )
			         .ifPresent( ContainerViewElement::clearChildren );
		}
	}

	private boolean isSingleComponent( EntityViewCommand command ) {
		return Boolean.TRUE.equals( command.getExtension( "singleComponent", Boolean.class ) );
	}

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( TextWebComponentResources.NAME );
	}

	/**
	 * Holder to ensure nested validation of the individual models.
	 */
	@RequiredArgsConstructor
	static class ModelsHolder
	{
		@Valid
		@Getter
		private final WebCmsComponentModel[] models;

		boolean contains( WebCmsComponent component ) {
			return Arrays.stream( models ).anyMatch( m -> component.getObjectId().equals( m.getObjectId() ) );
		}
	}

	/**
	 * Dummy link builder implementation that ensures update links are rendered in the same page.
	 */
	@RequiredArgsConstructor
	private static class InnerComponentLinkBuilder implements EntityLinkBuilder
	{
		private final String baseUrl;

		@Override
		public String overview() {
			return null;
		}

		@Override
		public String create() {
			return null;
		}

		@Override
		public String update( Object entity ) {
			return UriComponentsBuilder.fromUriString( baseUrl )
			                           .queryParam( "componentObjectId", ( (WebCmsObject) entity ).getObjectId() )
			                           .toUriString();
		}

		@Override
		public String delete( Object entity ) {
			return null;
		}

		@Override
		public String view( Object entity ) {
			return null;
		}

		@Override
		public String associations( Object entity ) {
			return null;
		}

		@Override
		public EntityLinkBuilder asAssociationFor( EntityLinkBuilder sourceLinkBuilder, Object sourceEntity ) {
			return null;
		}
	}
}
